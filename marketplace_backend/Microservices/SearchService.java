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

    public String searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "ERROR: Search keyword cannot be empty.";
        }

        List<ProductEntity> products = productDao.searchByName(keyword);
        
        JSONArray resultsArray = new JSONArray();
        for (ProductEntity p : products) {
            JSONObject productJson = new JSONObject();
            productJson.put("productId", p.getProductId());
            productJson.put("name", p.getName());
            productJson.put("price", p.getPrice());
            productJson.put("sellerId", p.getSellerId());
            productJson.put("status", p.getStatus());
            resultsArray.put(productJson);
        }

        JSONObject response = new JSONObject();
        response.put("results", resultsArray);
        response.put("count", resultsArray.length());

        return "SUCCESS " + response.toString();
    }
}