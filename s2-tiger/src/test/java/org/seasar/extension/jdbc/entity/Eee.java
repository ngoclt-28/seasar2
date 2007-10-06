/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.extension.jdbc.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

/**
 * @author koichik
 */
@Entity
public class Eee {

    /**
     * 
     */
    @Id
    public Integer id;

    /**
     * 
     */
    public String name;

    /**
     * 
     */
    public Integer fffId;

    /**
     * 
     */
    @OneToOne
    public Fff fff;

    /**
     * 
     */
    @Version
    public Long version;

    /**
     * 
     */
    @Column(insertable = false, updatable = false)
    public Timestamp lastUpdated;

}
