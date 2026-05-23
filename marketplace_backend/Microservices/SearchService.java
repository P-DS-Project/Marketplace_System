package Microservices;

import DAOs.ProductDAO;
import Entities.ProductEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class SearchService {
    private final ProductDAO productDao;

    public SearchService() {
        this.productDao = new ProductDAO();
    }

    private String formatResults(List<ProductEntity> products, int limit, int offset) {
        JSONArray resultsArray = new JSONArray();
        for (ProductEntity p : products) {
            JSONObject productJson = new JSONObject();
            productJson.put("productId", p.getProductId());
            productJson.put("name", p.getName());
            productJson.put("brand", p.getBrand());
            productJson.put("price", p.getPrice());
            productJson.put("sellerId", p.getSellerId());
            productJson.put("categoryId", p.getCategoryId());
            productJson.put("status", p.getStatus());
            productJson.put("description", p.getDescription());
            productJson.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
            if (p.getCreatedAt() != null) {
                productJson.put("createdAt", p.getCreatedAt().toString());
            }
            resultsArray.put(productJson);
        }

        JSONObject response = new JSONObject();
        response.put("results", resultsArray);
        response.put("count", resultsArray.length());
        response.put("limit", limit > 0 ? limit : 20);
        response.put("offset", offset >= 0 ? offset : 0);

        return "SUCCESS " + response.toString();
    }

    public String getProductsByCategory(int categoryId, String sortBy, String sortOrder, int limit, int offset) {
        if (categoryId <= 0) {
            return "ERROR: Invalid category ID.";
        }
        List<ProductEntity> products = productDao.advancedSearch(
            null, categoryId, null, null, null, null, null, null, sortBy, sortOrder, limit, offset
        );
        return formatResults(products, limit, offset);
    }

    public String searchByKeyword(String nameKeyword, String brandKeyword, String sortBy, String sortOrder, int limit, int offset) {
        if ((nameKeyword == null || nameKeyword.trim().isEmpty()) && (brandKeyword == null || brandKeyword.trim().isEmpty())) {
            return "ERROR: At least one search keyword (name or brand) must be provided.";
        }
        List<ProductEntity> products = productDao.advancedSearch(
            nameKeyword, null, null, null, brandKeyword, null, null, null, sortBy, sortOrder, limit, offset
        );
        return formatResults(products, limit, offset);
    }

    public String filterProducts(Integer categoryId, Double minPrice, Double maxPrice, String brand, Integer sellerId, String startDate, String endDate, String sortBy, String sortOrder, int limit, int offset) {
        List<ProductEntity> products = productDao.advancedSearch(
            null, categoryId, minPrice, maxPrice, brand, sellerId, startDate, endDate, sortBy, sortOrder, limit, offset
        );
        return formatResults(products, limit, offset);
    }
}