package com.example.portfolio.service;

import com.example.portfolio.dto.StockCreateRequest;
import com.example.portfolio.model.Stock;
import com.example.portfolio.model.User;
import com.example.portfolio.repository.StockRepository;
import com.example.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository repo;
    private final UserRepository userRepo;

    /** Oturumdaki kullanıcıya ait tüm hisseleri getirir. */
    public List<Stock> list() {
        User user = currentUser();
        return repo.findAllByOwnerId(user.getId());
    }

    /** Oturumdaki kullanıcı için yeni hisse kaydı oluşturur. */
    @Transactional
    public Stock create(StockCreateRequest r) {
        User user = currentUser();
        Stock s = Stock.builder()
                .symbol(r.symbol().toUpperCase())
                .quantity(r.quantity())
                .buyPrice(r.buyPrice())
                .currentPrice(r.buyPrice()) // ilk başta son fiyat = alış fiyatı
                .owner(user)
                .build();
        return repo.save(s);
    }

    /** Sadece sahibinin fiyatını güncelleyebilmesi için kontrol içerir. */
    @Transactional
    public Stock updatePrice(Long id, BigDecimal newPrice) {
        User user = currentUser();
        Stock s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));
        if (!s.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        s.setCurrentPrice(newPrice);
        return repo.save(s);
    }

    /** Sadece sahibi silebilir. */
    @Transactional
    public void delete(Long id) {
        User user = currentUser();
        Stock s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));
        if (!s.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        repo.deleteById(id);
    }

    /** SecurityContext'ten e-posta alıp kullanıcıyı bulur. */
    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String email = a.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
