/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nageoffer.ai.ragent.framework.mq.producer;

import java.util.function.Consumer;

/**
 * 消息队列生产者接口
 */
public interface MessageQueueProducer {

    /**
     * 发送消息
     *
     * @param exchange   目标 exchange
     * @param routingKey 路由 key
     * @param keys    业务 key，可用于幂等判断
     * @param bizDesc 业务描述，用于日志标识
     * @param body    业务载荷
     */
    void send(String exchange, String routingKey, String keys, String bizDesc, Object body);

    /**
     * 发送事务消息
     * <p>
     * 流程：执行本地事务 → 发送消息
     * <p>
     * 本地事务和消息发送在同一个事务中，如果本地事务失败则消息不发送
     *
     * @param exchange        目标 exchange
     * @param routingKey      路由 key
     * @param keys            业务 key
     * @param bizDesc         业务描述
     * @param body            业务载荷
     * @param localTransaction 本地事务逻辑，执行成功后发送消息；抛异常则回滚
     */
    void sendInTransaction(String exchange, String routingKey, String keys, String bizDesc, Object body,
                           Consumer<Object> localTransaction);
}
