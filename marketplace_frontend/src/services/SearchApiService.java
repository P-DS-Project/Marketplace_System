package services;

import models.Product;
import network.SocketClient;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class SearchApiService {

    private final SocketClient client = SocketClient.getInstance();

    public List<Product> getProductsByCategory(int categoryId, String sortBy, String sortOrder, int limit, int offset) {
        JSONObject payload = new JSONObject();
        payload.put("categoryId", categoryId);
        payload.put("sortBy", sortBy);
        payload.put("sortOrder", sortOrder);
        payload.put("limit", limit);
        payload.put("offset", offset);

        String response = client.sendRequest("SEARCH", "GET_PRODUCTS_BY_CATEGORY", payload);
        return parseResults(response);
    }

    public List<Product> searchByKeyword(String keyword, String brand, String sortBy, String sortOrder, int limit, int offset) {
        JSONObject payload = new JSONObject();
        if (keyword != null && !keyword.isEmpty()) payload.put("keyword", keyword);
        if (brand != null && !brand.isEmpty()) payload.put("brand", brand);
        payload.put("sortBy", sortBy);
        payload.put("sortOrder", sortOrder);
        payload.put("limit", limit);
        payload.put("offset", offset);

        String response = client.sendRequest("SEARCH", "SEARCH_BY_KEYWORD", payload);
        return parseResults(response);
    }

    /**
     * Unified filter/search method with optional keyword parameter.
     */
    public List<Product> filterProducts(String keyword, Integer categoryId, Double minPrice, Double maxPrice, String brand,
                                        Integer sellerId, String startDate, String endDate,
                                        String sortBy, String sortOrder, int limit, int offset) {
        JSONObject payload = new JSONObject();
        if (keyword != null && !keyword.isEmpty()) payload.put("keyword", keyword);
        if (categoryId != null) payload.put("categoryId", categoryId);
        if (minPrice != null) payload.put("minPrice", minPrice);
        if (maxPrice != null) payload.put("maxPrice", maxPrice);
        if (brand != null && !brand.isEmpty()) payload.put("brand", brand);
        if (sellerId != null) payload.put("sellerId", sellerId);
        if (startDate != null && !startDate.isEmpty()) payload.put("startDate", startDate);
        if (endDate != null && !endDate.isEmpty()) payload.put("endDate", endDate);
        payload.put("sortBy", sortBy);
        payload.put("sortOrder", sortOrder);
        payload.put("limit", limit);
        payload.put("offset", offset);

        // Use keyword search if keyword is provided, otherwise filter
        String action = (keyword != null && !keyword.isEmpty()) ? "SEARCH_BY_KEYWORD" : "FILTER_PRODUCTS";
        String response = client.sendRequest("SEARCH", action, payload);
        return parseResults(response);
    }

    private List<Product> parseResults(String response) {
        List<Product> products = new ArrayList<>();
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            JSONObject body = SocketClient.getResponseBody(response);
            JSONArray arr = body.optJSONArray("results");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Product p = new Product();
                    p.setProductId(obj.optInt("productId", 0));
                    p.setSellerId(obj.optInt("sellerId", 0));
                    p.setCategoryId(obj.optInt("categoryId", 0));
                    p.setName(obj.optString("name", ""));
                    p.setBrand(obj.optString("brand", ""));
                    p.setDescription(obj.optString("description", ""));
                    p.setPrice(obj.optDouble("price", 0));
                    p.setStatus(obj.optString("status", ""));
                    p.setCreatedAt(obj.optString("createdAt", ""));
                    p.setImageUrl(obj.optString("imageUrl", ""));
                    products.add(p);
                }
            }
        }
        return products;
    }
}
