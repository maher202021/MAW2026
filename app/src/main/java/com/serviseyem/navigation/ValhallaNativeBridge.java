package com.serviseyem.navigation;

import com.valhalla.valhalla.ValhallaActor;

/**
 * 🌉 ValhallaNativeBridge
 * جسر Java الصريح لاستدعاء محرك Valhalla C++ الأصلي
 * يتجاوز قيود Kotlin Metadata الداخلية مع الحفاظ على الأداء الفائق والربط المباشر
 */
public class ValhallaNativeBridge {

    private final ValhallaActor actor;

    public ValhallaNativeBridge(String configPath) {
        this.actor = new ValhallaActor(configPath);
    }

    public String route(String requestJson) {
        if (actor == null) {
            throw new IllegalStateException("ValhallaActor is not initialized");
        }
        return actor.route(requestJson);
    }

    public void close() {
        if (actor != null) {
            try {
                actor.close();
            } catch (Exception ignored) {
            }
        }
    }
}
