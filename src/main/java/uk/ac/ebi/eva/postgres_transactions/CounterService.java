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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounterService {

    private CounterRepository repository;

    public CounterService(CounterRepository repository) {
        this.repository = repository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CounterEntity reserveNewCounter() throws InterruptedException {
        CounterEntity highestCounter = repository.findFirstByOrderByCounterDesc();
        System.out.println("max counter: " + highestCounter.getCounter());
        System.out.println("waiting before update (within transaction)");
        Thread.sleep(5000);
        CounterEntity newCounter = new CounterEntity(highestCounter.getCounter());
        System.out.println("inserting counter=" + newCounter.getCounter());
        repository.save(newCounter);
        return newCounter;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void reset() {
        repository.deleteAll();
        repository.save(new CounterEntity(1));
    }
}
