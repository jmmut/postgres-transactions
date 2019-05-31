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
        if (args.length != 1 && args.length != 2) {
            System.out.println("this program simulates a race condition of "
                               + "'insert counter +1 from (select max(counter) counter)'. Run it in different "
                               + "terminals at the same time.\nneed 1 or 2 parameters, the url in "
                               + "format jdbc:postgresql://localhost:port/db?user=fred&password=secret and 'reset' to reset the DB");
            return;
        }
        String url = args[0];
        Connection conn = DriverManager.getConnection(url);
        if (args.length == 2) {
            if (!args[1].equals("reset")) {
                System.out.println("the second parameter can only be 'reset'");
                return;
            }
            conn.prepareStatement("delete from transaction_test where counter > 1").executeUpdate();
            conn.close();
            System.out.println("table reset to have only 1 row with counter=1");
        } else {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            PreparedStatement preparedQuery = conn.prepareStatement("select max(counter) counter from transaction_test");
            ResultSet resultSet = preparedQuery.executeQuery();
            resultSet.next();
            long counter = resultSet.getLong("counter");
            System.out.println("max counter: " + counter);
            System.out.println("waiting before update (within transaction)");
            Thread.sleep(2000);
            long incrementedCounter = counter + 1;
            System.out.println("inserting counter=" + incrementedCounter);
            PreparedStatement preparedInsert = conn.prepareStatement("insert into transaction_test values (?)");
            preparedInsert.setLong(1, incrementedCounter);
            preparedInsert.executeUpdate();
            conn.commit();
            conn.close();
            System.out.println("connection committed and closed");
        }
    }
}
