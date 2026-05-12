package services;

import models.Product;
import network.SocketClient;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class ProductApiService {

    private final SocketClient client = SocketClient.getInstance();

    public String addProduct(int sellerId, int categoryId, String name, double price, String description) {
        return addProduct(sellerId, categoryId, name, price, description, null, null);
    }

    public String addProduct(int sellerId, int categoryId, String name, double price, String description, String brand, String imageUrl) {
        JSONObject payload = new JSONObject();
        payload.put("sellerId", sellerId);
        payload.put("categoryId", categoryId);
        payload.put("name", name);
        payload.put("price", price);
        payload.put("description", description);
        if (brand != null && !brand.isEmpty()) payload.put("brand", brand);
        if (imageUrl != null && !imageUrl.isEmpty()) payload.put("imageUrl", imageUrl);

        String response = client.sendRequest("PRODUCT", "ADD", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200 || code == 201) {
            return body.optString("message", "Product added successfully");
        }
        return "ERROR: " + body.optString("error", "Failed to add product");
    }

    public List<Product> getProductsBySeller(int sellerId) {
        JSONObject payload = new JSONObject();
        payload.put("sellerId", sellerId);

        String response = client.sendRequest("PRODUCT", "GET_PRODUCTS_BY_SELLER", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        List<Product> products = new ArrayList<>();
        if (code == 200 && body.has("products")) {
            JSONArray arr = body.getJSONArray("products");
            for (int i = 0; i < arr.length(); i++) {
                products.add(parseProduct(arr.getJSONObject(i)));
            }
        }
        return products;
    }

    public Product getProductDetails(int productId) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);

        String response = client.sendRequest("PRODUCT", "GET_PRODUCT_DETAILS", payload);
        int code = SocketClient.getStatusCode(response);
        if (code == 200) {
            return parseProduct(SocketClient.getResponseBody(response));
        }
        return null;
    }

    public String updateProduct(int productId, String name, double price, String description) {
        return updateProduct(productId, name, price, description, null, null);
    }

    public String updateProduct(int productId, String name, double price, String description, String brand, String imageUrl) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);
        payload.put("name", name);
        payload.put("price", price);
        payload.put("description", description);
        if (brand != null) payload.put("brand", brand);
        if (imageUrl != null) payload.put("imageUrl", imageUrl);

        String response = client.sendRequest("PRODUCT", "UPDATE_PRODUCT", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) {
            return body.optString("message", "Product updated");
        }
        return "ERROR: " + body.optString("error", "Failed to update product");
    }

    public String updateProductStatus(int productId, String status) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);
        payload.put("status", status);

        String response = client.sendRequest("PRODUCT", "UPDATE_PRODUCT_STATUS", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) {
            return body.optString("message", "Status updated");
        }
        return "ERROR: " + body.optString("error", "Failed to update status");
    }

    public String removeProduct(int productId) {
        JSONObject payload = new JSONObject();
        payload.put("productId", productId);

        String response = client.sendRequest("PRODUCT", "REMOVE_PRODUCT", payload);
        int code = SocketClient.getStatusCode(response);
        JSONObject body = SocketClient.getResponseBody(response);

        if (code == 200) {
            return body.optString("message", "Product removed");
        }
        return "ERROR: " + body.optString("error", "Failed to remove product");
    }

    private Product parseProduct(JSONObject json) {
        Product p = new Product();
        p.setProductId(json.optInt("productId", 0));
        p.setSellerId(json.optInt("sellerId", 0));
        p.setCategoryId(json.optInt("categoryId", 0));
        p.setName(json.optString("name", ""));
        p.setBrand(json.optString("brand", ""));
        p.setDescription(json.optString("description", ""));
        p.setPrice(json.optDouble("price", 0));
        p.setStatus(json.optString("status", ""));
        p.setCreatedAt(json.optString("createdAt", ""));
        p.setImageUrl(json.optString("imageUrl", ""));
        return p;
    }
}
