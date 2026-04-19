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

package com.nageoffer.ai.ragent.infra.embedding;

import com.nageoffer.ai.ragent.infra.enums.ModelProvider;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

@Service
public class BaiLianEmbeddingClient extends AbstractOpenAIStyleEmbeddingClient {

    public BaiLianEmbeddingClient(OkHttpClient syncHttpClient) {
        super(syncHttpClient);
    }

    @Override
    public String provider() {
        return ModelProvider.BAI_LIAN.getId();
    }

    @Override
    protected void customizeRequestBody(com.google.gson.JsonObject body, com.nageoffer.ai.ragent.infra.model.ModelTarget target) {
        // 百炼 text-embedding-v3 支持的 dimension 为 [64, 128, 256, 512, 768, 1024]
        // AbstractOpenAIStyleEmbeddingClient 的 doEmbed 方法中可能已经添加了 dimensions 字段
        // 这里覆盖掉父类的 dimensions，改为 dimension
        if (body.has("dimensions")) {
            body.remove("dimensions");
        }
        if (target.candidate().getDimension() != null) {
            body.addProperty("dimension", target.candidate().getDimension());
        }
    }

    @Override
    protected int maxBatchSize() {
        return 10; // 阿里百炼 text-embedding-v3 接口最大支持的批量大小为 10
    }
}
