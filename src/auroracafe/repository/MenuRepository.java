package auroracafe.repository;

import auroracafe.model.MenuItem;
import auroracafe.service.CafeService;
import java.util.List;

public class MenuRepository implements Repository<MenuItem> {
    private final CafeService cafeService;
    public MenuRepository(CafeService cafeService) { this.cafeService = cafeService; }
    public List<MenuItem> findAll() { return cafeService.findMenu("", "Tất cả"); }
    public MenuItem findById(int id) { return cafeService.findMenuItem(id); }
    public boolean insert(MenuItem item) {
        cafeService.addMenuItem(item.getName(), item.getCategory(), item.getPrice(), item.isAvailable(), item.getDescription(), item.getImageUrl());
        return true;
    }
    public boolean update(MenuItem item) {
        cafeService.updateMenuItem(item.getId(), item.getName(), item.getCategory(), item.getPrice(), item.isAvailable(), item.getDescription(), item.getImageUrl());
        return true;
    }
    public boolean delete(int id) { cafeService.deleteMenuItem(id); return true; }
    public List<MenuItem> search(String keyword) { return cafeService.findMenu(keyword, "Tất cả"); }
}
