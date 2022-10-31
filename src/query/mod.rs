/*
 * Copyright (C) 2021 Vaticle
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

use std::iter::once;
use std::sync::{Arc, Mutex};
use futures::{Stream, stream, StreamExt};
use typedb_protocol::{query_manager, transaction};
use crate::answer::ConceptMap;

use crate::common::error::MESSAGES;
use crate::common::Result;
use crate::rpc::builder::query_manager::{define_req, delete_req, insert_req, match_req, update_req, undefine_req};
use crate::rpc::transaction::TransactionRpc;

macro_rules! stream_concept_maps {
    ($self:ident, $req:ident, $res_part_kind:ident, $query_type_str:tt) => {
        $self.stream_answers($req).flat_map(|result: Result<query_manager::res_part::Res>| {
            match result {
                Ok(res_part) => {
                    match res_part {
                        $res_part_kind(x) => {
                            stream::iter(x.answers.into_iter().map(|cm| { ConceptMap::from_proto(cm) })).left_stream()
                        }
                        _ => {
                            stream::iter(once(Err(MESSAGES.client.missing_response_field.to_err(
                                vec![format!("query_manager_res_part.{}_res_part", $query_type_str).as_str()]
                            )))).right_stream()
                        }
                    }
                }
                Err(err) => { stream::iter(once(Err(err))).right_stream() }
            }
        })
    };
}

#[derive(Clone, Debug)]
pub struct QueryManager {
    tx: TransactionRpc
}

impl QueryManager {
    pub(crate) fn new(tx: &TransactionRpc) -> QueryManager {
        QueryManager { tx: tx.clone() }
    }

    pub async fn define(&mut self, query: &str) -> Result {
        self.single_call(define_req(query)).await.map(|_| ())
    }

    pub async fn delete(&mut self, query: &str) -> Result {
        self.single_call(delete_req(query)).await.map(|_| ())
    }

    pub fn insert(&mut self, query: &str) -> impl Stream<Item = Result<ConceptMap>> {
        let req = insert_req(query);
        stream_concept_maps!(self, req, query_manager::res_part::Res::InsertResPart, "insert")
    }

    // TODO: investigate performance impact of using BoxStream
    pub fn match_(&mut self, query: &str) -> impl Stream<Item = Result<ConceptMap>> {
        let req = match_req(query);
        stream_concept_maps!(self, req, query_manager::res_part::Res::MatchResPart, "match")
    }

    pub async fn undefine(&mut self, query: &str) -> Result {
        self.single_call(undefine_req(query)).await.map(|_| ())
    }

    pub fn update(&mut self, query: &str) -> impl Stream<Item = Result<ConceptMap>> {
        let req = update_req(query);
        stream_concept_maps!(self, req, query_manager::res_part::Res::UpdateResPart, "update")
    }

    async fn single_call(&mut self, req: transaction::Req) -> Result<query_manager::res::Res> {
        match self.tx.single(req).await?.res {
            Some(transaction::res::Res::QueryManagerRes(res)) => {
                res.res.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["res.query_manager_res"]))
            }
            _ => { Err(MESSAGES.client.missing_response_field.to_err(vec!["res.query_manager_res"])) }
        }
    }

    fn stream_answers(&mut self, req: transaction::Req) -> impl Stream<Item = Result<query_manager::res_part::Res>> {
        self.tx.stream(req).map(|result: Result<transaction::ResPart>| {
            match result {
                Ok(tx_res_part) => {
                    match tx_res_part.res {
                        Some(transaction::res_part::Res::QueryManagerResPart(res_part)) => {
                            res_part.res.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["res_part.query_manager_res_part"]))
                        }
                        _ => { Err(MESSAGES.client.missing_response_field.to_err(vec!["res_part.query_manager_res_part"])) }
                    }
                }
                Err(err) => { Err(err) }
            }
        })
    }
}
