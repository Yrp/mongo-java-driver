/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb;

import com.mongodb.CommandFailureException;
import com.mongodb.MongoSecurityException;
import org.junit.Before;
import org.junit.Test;

import static com.mongodb.AuthenticationMechanism.PLAIN;
import static com.mongodb.MongoCredential.createPlainCredential;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mongodb.Fixture.getCredentialList;
import static org.mongodb.Fixture.getMongoClient;
import static org.mongodb.Fixture.getMongoClientURI;
import static org.mongodb.Fixture.getPrimary;

public class PlainAuthenticationTest {
    @Before
    public void setUp() {
        assumeTrue(!getCredentialList().isEmpty() && getCredentialList().get(0).getMechanism().equals(PLAIN));
    }

    @Test(expected = CommandFailureException.class)
    public void testUnsuccessfulAuthorization() throws InterruptedException {
        MongoClient client = MongoClients.create(getPrimary());
        MongoCollection<Document> collection = client.getDatabase(getMongoClientURI().getDatabase()).getCollection("test");
        try {
            collection.find().count();
        } finally {
            client.close();
        }
    }

    @Test
    public void testSuccessfulAuthenticationAndAuthorization() {
        MongoCollection<Document> collection = getMongoClient().getDatabase(getMongoClientURI().getDatabase()).getCollection("test");
        assertTrue(collection.find().count() >= 0); // Really just asserting that the query doesn't throw any security-related exceptions
    }

    @Test(expected = MongoSecurityException.class)
    public void testUnsuccessfulAuthentication() throws InterruptedException {
        MongoClient client = MongoClients.create(getPrimary(), asList(createPlainCredential("wrongUserName", "$external",
                                                                                            "wrongPassword".toCharArray())));
        MongoCollection<Document> collection = client.getDatabase(getMongoClientURI().getDatabase()).getCollection("test");
        try {
            collection.find().count();
        } finally {
            client.close();
        }
    }
}
