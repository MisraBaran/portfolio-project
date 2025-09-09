# Portföy Takip Uygulaması

Bu proje, kullanıcıların sahip oldukları hisseleri kaydedip **alış fiyatı**, **anlık satış fiyatı** ve **kâr/zarar durumunu** takip edebilmelerini sağlayan bir web uygulamasıdır.  
Amaç, basit bir simülasyonla portföy yönetimi deneyimi sunmak ve ileride gerçek borsa verileriyle entegrasyona uygun bir altyapı oluşturmaktır.

---

## Özellikler

- **Kullanıcı Girişi:**  
  Kullanıcı e-posta ve şifre ile giriş yapar.

- **Hisse Kaydetme:**  
  Kullanıcı “Portföyüm” sayfasına sahip olduğu hisseleri ekleyebilir:  

- **Fiyat Güncellemeleri:**  
  - Sistem her hisse için anlık satış fiyatını api den çekereke gerçek verileri gösterir.

- **Portföy Ekranı:**  
  Kullanıcının eklediği hisseler tablo halinde gösterilir

- **Kâr/Zarar Hesabı:**  
  Kullanıcının kar/zarar durumları hesaplanır.

---

## Kullanılan Teknolojiler

- **Frontend:** React  
- **Backend:** Java (Spring Boot)  
- **Veritabanı:** MySQL  

---

##  Kurulum ve Çalıştırma

### 1. Repoyu Klonla
```bash
git clone https://github.com/MisraBaran/portfolio-project.git
cd portfolio-project

