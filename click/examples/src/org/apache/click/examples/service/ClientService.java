/*
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
package org.apache.click.examples.service;

import java.util.List;

import org.apache.click.examples.domain.Client;
import org.apache.click.extras.cayenne.CayenneTemplate;

import org.apache.cayenne.query.SelectQuery;
import org.springframework.stereotype.Component;

/**
 * Provides a Client Service.
 *
 * @see Client
 *
 * @author Malcolm Edgar
 */
@Component
public class ClientService extends CayenneTemplate {

    @SuppressWarnings("unchecked")
    public List<Client> getClients() {
        SelectQuery query = new SelectQuery(Client.class);
        query.addOrdering("db:id", true);
        return performQuery(query);
    }

    public Client getClient(Object id) {
        return (Client) getObjectForPK(Client.class, id);
    }

    public void saveClient(Client client) {
        if (client.getObjectContext() == null) {
            registerNewObject(client);
        }
        commitChanges();
    }

    public Client createClientInNestedContext() {
        return (Client) getDataContext().createChildDataContext().
            createAndRegisterNewObject(Client.class);
    }
}
