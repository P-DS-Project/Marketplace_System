package Handlers;

import Microservices.SearchService;
import org.json.JSONObject;

public class SearchHandler implements ServiceHandler {

    private final SearchService searchService;

    public SearchHandler() {
        this.searchService = new SearchService();
    }

    private String formatResponse(String result) {
        if (result.startsWith("ERROR")) {
            return "400 {\"error\":\"" + result + "\"}";
        } else if (result.startsWith("SUCCESS ")) {
            return "200 " + result.substring(8);
        }
        return "200 " + result;
    }

    @Override
    public String handleRequest(String action, String jsonPayload) {
        JSONObject json;
        try {
            json = new JSONObject(jsonPayload != null ? jsonPayload : "{}");
        } catch (Exception e) {
            return "400 {\"error\":\"Invalid request: " + e.getMessage() + "\"}";
        }

        String sortBy = json.optString("sortBy", "created_at");
        String sortOrder = json.optString("sortOrder", "DESC");
        int limit = json.optInt("limit", 20);
        int offset = json.optInt("offset", 0);

        switch (action.toUpperCase()) {
            case "GET_PRODUCTS_BY_CATEGORY": {
                int categoryId = json.optInt("categoryId", -1);
                String result = searchService.getProductsByCategory(categoryId, sortBy, sortOrder, limit, offset);
                return formatResponse(result);
            }

            case "SEARCH_BY_KEYWORD": {
                String nameKeyword = json.optString("keyword", json.optString("query", json.optString("name", null)));
                if (nameKeyword != null && nameKeyword.isEmpty()) nameKeyword = null;
                
                String brandKeyword = json.optString("brand", null);
                if (brandKeyword != null && brandKeyword.isEmpty()) brandKeyword = null;

                String result = searchService.searchByKeyword(nameKeyword, brandKeyword, sortBy, sortOrder, limit, offset);
                return formatResponse(result);
            }

            case "FILTER_PRODUCTS": {
                Integer categoryId = json.has("categoryId") ? json.optInt("categoryId") : null;
                Double minPrice = json.has("minPrice") ? json.optDouble("minPrice") : null;
                Double maxPrice = json.has("maxPrice") ? json.optDouble("maxPrice") : null;
                
                String brand = json.optString("brand", null);
                if (brand != null && brand.isEmpty()) brand = null;
                
                Integer sellerId = json.has("sellerId") ? json.optInt("sellerId") : null;
                
                String startDate = json.optString("startDate", null);
                if (startDate != null && startDate.isEmpty()) startDate = null;
                
                String endDate = json.optString("endDate", null);
                if (endDate != null && endDate.isEmpty()) endDate = null;

                String result = searchService.filterProducts(
                    categoryId, minPrice, maxPrice, brand, sellerId, startDate, endDate, sortBy, sortOrder, limit, offset
                );
                return formatResponse(result);
            }

            default:
                return "400 {\"error\":\"Unknown Search Action: " + action + "\"}";
        }
    }
}
