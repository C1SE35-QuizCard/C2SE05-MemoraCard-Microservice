package com.microservices.apigateway.configuration;

public class UrlFilter {
//    public static String[] authEndpoints = {
//            "/auth/user-info",
//            "/auth/logout",
//            "/auth/update-password",
//            "/home/data",
//            "/home/admin",
//            "/set/count-set",
//            "/set/count-set-in-current-date",
//            "/set/create-new-set",
//            "/set/update-set",
//            "/set/delete-set/",
//            "/flashcards/create-new-flashcard",
//            "/flashcards/update-flashcard",
//            "/flashcards/delete-flashcard",
//            "/folder/create-new-folder",
//            "/folder/update-folder",
//            "/folder/delete-folder",
//            "/folder/user",
//            "/collection/create-new-collection",
//            "/collection/delete-collection",
//            "/deadline/create-deadline",
//            "/deadline/update-deadline",
//            "/deadline/delete-deadline/",
//            "/category-subscription/current-benefit",
//            "/category-subscription/current-subscription",
//            "/flashcard-settings/update",
//            "/flashcard-settings/",
//            "/flashcard-settings/sort",
//            "/progress/user/set/",
//            "/progress/user/analysis/set/",
//            "/progress/user/assign-progress",
//            "/progress/user/reset-progress/",
//            "/users/**",
//            "/progress/user/**",
//            "/category/create",
//            "/category/update",
//            "/category/delete",
//            "/notification/**",
//            "/streak-learning/**",
//            "/srs-progress/**",
//            "/setting-progress/**",
//            "/set/filter",
//            "/set/count-set-in-current-date",
//            "/set/get-current-sets-by-settings",
//    };

    // vứt cmn cái cũ rồi
    public static String[] authEndpoints = {
            "/notification/**",
            "/notification-testing/**",
    };

    public static String[] publicEndpoints = new String[]
            {
                    "/api/ws",
                    "/api/ws/**",
                    "/api/auth/**",
                    "/api/v1/ka",
                    "/api/v1/ka/**",
                    "/api/v1/public/**",
                    "/api/v1/auth/signup",
                    "/api/v1/auth/login",
                    "/api/v1/auth/logout-old",
                    "/api/v1/auth/logout",
                    "/api/v1/auth/logout-all",
            };
}
