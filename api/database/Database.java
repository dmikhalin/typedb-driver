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

package com.vaticle.typedb.client.api.database;

import javax.annotation.CheckReturnValue;
import java.util.Optional;
import java.util.Set;

public interface Database {

    @CheckReturnValue
    String name();

    @CheckReturnValue
    String schema();

    @CheckReturnValue
    String typeSchema();

    @CheckReturnValue
    String ruleSchema();

    void delete();

    interface Cluster extends Database {

        @CheckReturnValue
        Set<? extends Replica> replicas();

        @CheckReturnValue
        Optional<? extends Replica> primaryReplica();

        @CheckReturnValue
        Replica preferredReplica();
    }

    interface Replica {

        @CheckReturnValue
        Cluster database();

        @CheckReturnValue
        String address();

        @CheckReturnValue
        boolean isPrimary();

        @CheckReturnValue
        boolean isPreferred();

        @CheckReturnValue
        long term();
    }
}
