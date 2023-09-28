#
# Copyright (C) 2022 Vaticle
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.connection.transaction import TypeDBTransaction


class Rule(ABC):

    @property
    @abstractmethod
    def label(self) -> str:
        """
        Retrieves the unique label of the rule.

        :return:

        Examples:
        ---------
        ::

            rule.get_label()
        """
        pass

    @abstractmethod
    def set_label(self, transaction: TypeDBTransaction, new_label: str) -> None:
        """
        Renames the label of the rule. The new label must remain unique.

        :param transaction: The current ``Transaction``
        :param new_label: The new label to be given to the rule
        :return:

        Examples:
        ---------
        ::

            rule.set_label(transaction, new_label)
        """
        pass

    @property
    @abstractmethod
    def when(self) -> str:
        """
        Retrieves the statements that constitute the 'when' of the rule.

        :return:

        Examples:
        ---------
        ::

            rule.get_when()
        """
        pass

    @property
    @abstractmethod
    def then(self) -> str:
        """
        Retrieves the single statement that constitutes the 'then' of the rule.

        :return:

        Examples:
        ---------
        ::

            rule.get_then()
        """
        pass

    @abstractmethod
    def delete(self, transaction: TypeDBTransaction) -> None:
        """

        :param transaction: The current ``Transaction``
        :return:

        Examples:
        ---------
        ::

            rule.delete(transaction)
        """
        pass

    @abstractmethod
    def is_deleted(self, transaction: TypeDBTransaction) -> bool:
        """

        :param transaction: The current ``Transaction``
        :return:

        Examples:
        ---------
        ::

            rule.is_deleted(transaction)
        """
        pass
