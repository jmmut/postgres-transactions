/*
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.postgres_transactions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Application {

    public static void main(String[] args) throws InterruptedException, SQLException {
//        System.out.println("args provided: " + String.join(", ", args));
        if (args.length != 1) {
            System.out.println("need 1 parameter, the url in format jdbc:postgresql://localhost:port/db?user=fred&password=secret");
            return;
        }
        String url = args[0];
        Connection conn = DriverManager.getConnection(url);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        conn.setAutoCommit(false);
        PreparedStatement s = conn.prepareStatement("select max(counter) counter from transaction_test");
        ResultSet resultSet = s.executeQuery();
        resultSet.next();
        long counter = resultSet.getLong("counter");
        System.out.println(counter);
        conn.prepareStatement("insert into transaction_test values (?)").setLong(1, counter +1);
        conn.commit();
        conn.close();
    }

}
