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
import java.util.List;

public interface DatabaseManager {

    @CheckReturnValue
    Database get(String name);

    @CheckReturnValue
    boolean contains(String name);
    // TODO: Return type should be 'Database' but right now that would require 2 server calls in Cluster

    void create(String name);

    @CheckReturnValue
    List<? extends Database> all();

    interface Cluster extends DatabaseManager {

        @Override
        @CheckReturnValue
        Database.Cluster get(String name);

        @Override
        @CheckReturnValue
        List<Database.Cluster> all();
    }
}
