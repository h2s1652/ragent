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

import cn.hutool.core.util.StrUtil;
import com.nageoffer.ai.ragent.framework.mq.MessageWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 基于 RabbitMQ 的消息生产者
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMQProducerAdapter implements MessageQueueProducer {

    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void send(String exchange, String routingKey, String keys, String bizDesc, Object body) {
        keys = StrUtil.isEmpty(keys) ? UUID.randomUUID().toString() : keys;

        Message<MessageWrapper<Object>> message = MessageBuilder
                .withPayload(MessageWrapper.builder().keys(keys).body(body).build())
                .build();

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Throwable ex) {
            log.error("[生产者] {} - 消息发送失败，exchange: {}, routingKey: {}, keys: {}", bizDesc, exchange, routingKey, keys, ex);
            throw ex;
        }

        log.info("[生产者] {} - 发送成功，exchange: {}, routingKey: {}, keys: {}", bizDesc, exchange, routingKey, keys);
    }

    @Override
    public void sendInTransaction(String exchange, String routingKey, String keys, String bizDesc, Object body,
                                  Consumer<Object> localTransaction) {
        final String finalKeys = StrUtil.isEmpty(keys) ? UUID.randomUUID().toString() : keys;

        transactionTemplate.executeWithoutResult(status -> {
            try {
                localTransaction.accept(body);
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }

            Message<MessageWrapper<Object>> message = MessageBuilder
                    .withPayload(MessageWrapper.builder().keys(finalKeys).body(body).build())
                    .build();

            try {
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
                log.info("[生产者] {} - 事务消息发送成功，exchange: {}, routingKey: {}, keys: {}", bizDesc, exchange, routingKey, finalKeys);
            } catch (Throwable ex) {
                log.error("[生产者] {} - 事务消息发送失败，exchange: {}, routingKey: {}, keys: {}", bizDesc, exchange, routingKey, finalKeys, ex);
                throw ex;
            }
        });
    }
}
