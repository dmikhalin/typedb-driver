/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.vaticle.typedb.client.common.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_PASSWORD_CREDENTIAL_EXPIRED;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_TOKEN_CREDENTIAL_INVALID;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.RPC_METHOD_UNAVAILABLE;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.UNABLE_TO_CONNECT;

public class TypeDBClientException extends RuntimeException {

    // TODO: propagate exception from the server side in a less-brittle way
    private static final String CLUSTER_REPLICA_NOT_PRIMARY_ERROR_CODE = "[RPL01]";
    private static final String CLUSTER_TOKEN_CREDENTIAL_INVALID_ERROR_CODE = "[CLS08]";
    private static final String CLUSTER_PASSWORD_CREDENTIAL_EXPIRED_ERROR_CODE = "[CLS10]";

    @Nullable
    private final ErrorMessage errorMessage;

    public TypeDBClientException(ErrorMessage error, Object... parameters) {
        super(error.message(parameters));
        assert !getMessage().contains("%s");
        this.errorMessage = error;
    }

    public TypeDBClientException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = null;
    }

    public static TypeDBClientException of(StatusRuntimeException sre) {
        if (isUnimplementedMethod(sre)) {
            return new TypeDBClientException(RPC_METHOD_UNAVAILABLE, sre.getStatus().getDescription());
        } else if (isRstStream(sre)) {
            return new TypeDBClientException(UNABLE_TO_CONNECT);
        } else if (isReplicaNotPrimary(sre)) {
            return new TypeDBClientException(CLUSTER_REPLICA_NOT_PRIMARY);
        } else if (isTokenCredentialInvalid(sre)) {
            return new TypeDBClientException(CLUSTER_TOKEN_CREDENTIAL_INVALID);
         } else if (isPasswordCredentialExpired(sre)) {
            return new TypeDBClientException(CLUSTER_PASSWORD_CREDENTIAL_EXPIRED);
        } else {
            return new TypeDBClientException(sre.getStatus().getDescription(), sre);
        }
    }

    private static boolean isRstStream(StatusRuntimeException statusRuntimeException) {
        // "Received Rst Stream" occurs if the server is in the process of shutting down.
        return statusRuntimeException.getStatus().getCode() == Status.Code.UNAVAILABLE ||
                statusRuntimeException.getStatus().getCode() == Status.Code.UNKNOWN ||
                statusRuntimeException.getMessage().contains("Received Rst Stream");
    }

    private static boolean isReplicaNotPrimary(StatusRuntimeException statusRuntimeException) {
        return statusRuntimeException.getStatus().getCode() == Status.Code.INTERNAL &&
                statusRuntimeException.getStatus().getDescription() != null &&
                statusRuntimeException.getStatus().getDescription().contains(CLUSTER_REPLICA_NOT_PRIMARY_ERROR_CODE);
    }

    private static boolean isTokenCredentialInvalid(StatusRuntimeException statusRuntimeException) {
        return statusRuntimeException.getStatus().getCode() == Status.Code.UNAUTHENTICATED &&
                statusRuntimeException.getStatus().getDescription() != null &&
                statusRuntimeException.getStatus().getDescription().contains(CLUSTER_TOKEN_CREDENTIAL_INVALID_ERROR_CODE);
    }

    private static boolean isPasswordCredentialExpired(StatusRuntimeException statusRuntimeException) {
        return statusRuntimeException.getStatus().getCode() == Status.Code.UNAUTHENTICATED &&
                statusRuntimeException.getStatus().getDescription() != null &&
                statusRuntimeException.getStatus().getDescription().contains(CLUSTER_PASSWORD_CREDENTIAL_EXPIRED_ERROR_CODE);
    }

    private static boolean isUnimplementedMethod(StatusRuntimeException statusRuntimeException) {
        return statusRuntimeException.getStatus().getCode() == Status.Code.UNIMPLEMENTED;
    }

    public String getName() {
        return this.getClass().getName();
    }

    @Nullable
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
