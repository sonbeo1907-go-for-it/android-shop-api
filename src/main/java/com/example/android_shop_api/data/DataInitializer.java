package com.example.android_shop_api.data;

import com.example.android_shop_api.entity.phone.Phone;
import com.example.android_shop_api.entity.phone.PhoneOption;
import com.example.android_shop_api.entity.phone.PhoneOptionType;
import com.example.android_shop_api.repository.phone.PhoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(DataInitializer.class);

    /*
     * Đây là tỷ giá cố định dành cho dữ liệu demo,
     * không phải tỷ giá thời gian thực.
     */
    private static final BigDecimal USD_TO_VND =
            BigDecimal.valueOf(25_000L);

    private static final BigDecimal TWO_MILLION =
            BigDecimal.valueOf(2_000_000L);

    private static final BigDecimal TWO_AND_HALF_MILLION =
            BigDecimal.valueOf(2_500_000L);

    private static final BigDecimal THREE_MILLION =
            BigDecimal.valueOf(3_000_000L);

    private static final BigDecimal FOUR_MILLION =
            BigDecimal.valueOf(4_000_000L);

    private static final BigDecimal FIVE_MILLION =
            BigDecimal.valueOf(5_000_000L);

    private static final BigDecimal EIGHT_MILLION =
            BigDecimal.valueOf(8_000_000L);

    /*
     * Vị trí tính từ 0 trong danh sách seed.
     */
    private static final Set<Integer> FEATURED_POSITIONS =
            Set.of(0, 1, 2, 3, 4, 10, 11, 18);

    private final PhoneRepository phoneRepository;

    public DataInitializer(
            PhoneRepository phoneRepository
    ) {
        this.phoneRepository = phoneRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (phoneRepository.count() > 0) {
            log.info(
                    "Phone seed skipped because the phones table already contains data."
            );
            return;
        }

        List<SeedPhone> seeds = createSeedPhones();
        List<Phone> phones = new ArrayList<>(seeds.size());

        for (int index = 0; index < seeds.size(); index++) {
            phones.add(toEntity(seeds.get(index), index));
        }

        phoneRepository.saveAll(phones);

        log.info(
                "Seeded {} phones and their options successfully.",
                phones.size()
        );
    }

    private Phone toEntity(
            SeedPhone seed,
            int index
    ) {
        Phone phone = new Phone();

        BigDecimal basePrice = convertUsdToVnd(seed.usdPrice());

        phone.setName(seed.name());
        phone.setSlug(toSlug(seed.name()));
        phone.setModel(seed.name());
        phone.setBrand(seed.brand());
        phone.setBasePrice(basePrice);
        phone.setOriginalPrice(null);
        phone.setShortDescription(seed.description());
        phone.setDescription(seed.description());
        phone.setThumbnailUrl(seed.image());

        /*
         * Data cũ chỉ có một ảnh cho mỗi sản phẩm.
         * Backend vẫn trả đúng kiểu List<String>.
         */
        phone.setImages(
                new ArrayList<>(List.of(seed.image()))
        );

        phone.setSpecifications(
                createSpecifications(seed)
        );

        /*
         * Tồn kho và soldCount là dữ liệu demo mới,
         * không giữ từ cấu trúc frontend cũ.
         */
        phone.setStockQuantity(
                15 + ((index * 7) % 31)
        );

        phone.setSoldCount(
                Math.max(50L, 500L - index * 13L)
        );

        phone.setFeatured(
                FEATURED_POSITIONS.contains(index)
        );

        phone.setActive(true);

        addDefaultOptions(phone, basePrice);

        return phone;
    }

    private Map<String, String> createSpecifications(
            SeedPhone seed
    ) {
        Map<String, String> specifications =
                new LinkedHashMap<>();

        specifications.put(
                "Thương hiệu",
                seed.brand()
        );

        specifications.put(
                "Hệ điều hành",
                seed.brand().equalsIgnoreCase("Apple")
                        ? "iOS"
                        : "Android"
        );

        specifications.put(
                "Tình trạng",
                "Mới"
        );

        specifications.put(
                "Bảo hành",
                "12 tháng"
        );

        return specifications;
    }

    private void addDefaultOptions(
            Phone phone,
            BigDecimal basePrice
    ) {
        addOption(
                phone,
                PhoneOptionType.COLOR,
                "Đen",
                BigDecimal.ZERO,
                phone.getThumbnailUrl(),
                1
        );

        addOption(
                phone,
                PhoneOptionType.COLOR,
                "Bạc",
                BigDecimal.ZERO,
                null,
                2
        );

        addOption(
                phone,
                PhoneOptionType.COLOR,
                "Xanh",
                BigDecimal.valueOf(500_000L),
                null,
                3
        );

        boolean premium =
                basePrice.compareTo(
                        BigDecimal.valueOf(25_000_000L)
                ) >= 0;

        if (premium) {
            addOption(
                    phone,
                    PhoneOptionType.RAM,
                    "12 GB",
                    BigDecimal.ZERO,
                    null,
                    1
            );

            addOption(
                    phone,
                    PhoneOptionType.RAM,
                    "16 GB",
                    THREE_MILLION,
                    null,
                    2
            );

            addOption(
                    phone,
                    PhoneOptionType.STORAGE,
                    "256 GB",
                    BigDecimal.ZERO,
                    null,
                    1
            );

            addOption(
                    phone,
                    PhoneOptionType.STORAGE,
                    "512 GB",
                    FOUR_MILLION,
                    null,
                    2
            );

            addOption(
                    phone,
                    PhoneOptionType.STORAGE,
                    "1 TB",
                    EIGHT_MILLION,
                    null,
                    3
            );
        } else {
            addOption(
                    phone,
                    PhoneOptionType.RAM,
                    "8 GB",
                    BigDecimal.ZERO,
                    null,
                    1
            );

            addOption(
                    phone,
                    PhoneOptionType.RAM,
                    "12 GB",
                    TWO_MILLION,
                    null,
                    2
            );

            addOption(
                    phone,
                    PhoneOptionType.STORAGE,
                    "128 GB",
                    BigDecimal.ZERO,
                    null,
                    1
            );

            addOption(
                    phone,
                    PhoneOptionType.STORAGE,
                    "256 GB",
                    TWO_AND_HALF_MILLION,
                    null,
                    2
            );

            addOption(
                    phone,
                    PhoneOptionType.STORAGE,
                    "512 GB",
                    FIVE_MILLION,
                    null,
                    3
            );
        }
    }

    private void addOption(
            Phone phone,
            PhoneOptionType type,
            String value,
            BigDecimal extraPrice,
            String imageUrl,
            int displayOrder
    ) {
        PhoneOption option = new PhoneOption();

        option.setType(type);
        option.setValue(value);
        option.setExtraPrice(extraPrice);
        option.setImageUrl(imageUrl);
        option.setActive(true);
        option.setDisplayOrder(displayOrder);

        phone.addOption(option);
    }

    private BigDecimal convertUsdToVnd(long usdPrice) {
        return BigDecimal
                .valueOf(usdPrice)
                .multiply(USD_TO_VND);
    }

    private String toSlug(String value) {
        String withoutAccent = Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return withoutAccent
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private List<SeedPhone> createSeedPhones() {
        return List.of(
                new SeedPhone(
                        "iPhone 15 Pro Max",
                        "Apple",
                        1349,
                        "/images/iphone-15-pro-max.jpg",
                        "Flagship mạnh mẽ nhất của Apple với chip A17 Pro."
                ),
                new SeedPhone(
                        "Samsung Galaxy S24 Ultra",
                        "Samsung",
                        1299,
                        "/images/samsung-s24-ultra.jpg",
                        "Đỉnh cao Android với bút S Pen và AI thông minh."
                ),
                new SeedPhone(
                        "Google Pixel 8 Pro",
                        "Google",
                        999,
                        "/images/google-pixel-8-pro.jpg",
                        "Trải nghiệm Android thuần khiết cùng camera AI đỉnh cao từ Google."
                ),
                new SeedPhone(
                        "Xiaomi 14 Ultra",
                        "Xiaomi",
                        1199,
                        "/images/xiaomi-14-ultra.jpg",
                        "Quái vật nhiếp ảnh kết hợp cùng ống kính Leica cao cấp."
                ),
                new SeedPhone(
                        "OnePlus 12",
                        "OnePlus",
                        799,
                        "/images/oneplus-12.jpg",
                        "Kẻ hủy diệt flagship với hiệu năng cực khủng và sạc siêu nhanh 100W."
                ),
                new SeedPhone(
                        "iPhone 15",
                        "Apple",
                        799,
                        "/images/iphone-15.jpg",
                        "Sự kết hợp hoàn hảo giữa kích thước nhỏ gọn và Dynamic Island tiện lợi."
                ),
                new SeedPhone(
                        "Samsung Galaxy S24 Plus",
                        "Samsung",
                        999,
                        "/images/samsung-s24-plus.jpg",
                        "Cân bằng hoàn hảo giữa màn hình lớn sắc nét và thời lượng pin ấn tượng."
                ),
                new SeedPhone(
                        "Oppo Find X7 Ultra",
                        "Oppo",
                        1099,
                        "/images/oppo-find-x7-ultra.jpg",
                        "Hệ thống camera kính tiềm vọng kép đầu tiên trên thế giới chỉnh chu bởi Hasselblad."
                ),
                new SeedPhone(
                        "Vivo X100 Pro",
                        "Vivo",
                        949,
                        "/images/vivo-x100-pro.jpg",
                        "Chuyên gia chụp chân dung với ống kính ZEISS và chip xử lý hình ảnh V3."
                ),
                new SeedPhone(
                        "Sony Xperia 1 VI",
                        "Sony",
                        1399,
                        "/images/sony-xperia-1-vi.jpg",
                        "Lựa chọn chuẩn \"Pro\" cho dân sáng tạo nội dung với khả năng zoom quang học liên tục."
                ),
                new SeedPhone(
                        "Asus ROG Phone 8 Pro",
                        "Asus",
                        1199,
                        "/images/asus-rog-8-pro.jpg",
                        "Quái vật Gaming Phone với RAM 24GB và hệ thống tản nhiệt AeroActive tối tân."
                ),
                new SeedPhone(
                        "Samsung Galaxy Z Fold5",
                        "Samsung",
                        1599,
                        "/images/samsung-z-fold5.jpg",
                        "Thiết kế màn hình gập cao cấp nâng tầm hiệu suất làm việc đa nhiệm."
                ),
                new SeedPhone(
                        "iPhone 15 Plus",
                        "Apple",
                        929,
                        "/images/iphone-15-plus.jpg",
                        "Màn hình lớn và thời lượng pin trâu bò nhất dòng iPhone 15 tiêu chuẩn."
                ),
                new SeedPhone(
                        "Samsung Galaxy S24",
                        "Samsung",
                        799,
                        "/images/samsung-s24.jpg",
                        "Flagship nhỏ gọn với đầy đủ tính năng Galaxy AI thông minh cao cấp."
                ),
                new SeedPhone(
                        "Xiaomi 14",
                        "Xiaomi",
                        899,
                        "/images/xiaomi-14.jpg",
                        "Thiết kế vừa vặn, cấu hình siêu mạnh với chip Snapdragon 8 Gen 3."
                ),
                new SeedPhone(
                        "Google Pixel 8",
                        "Google",
                        699,
                        "/images/google-pixel-8.jpg",
                        "Trải nghiệm Android mượt mà cùng camera nhiếp ảnh thuật toán thông minh."
                ),
                new SeedPhone(
                        "Oppo Reno12 Pro",
                        "Oppo",
                        649,
                        "/images/oppo-reno12-pro.jpg",
                        "Chuyên gia chân dung thế hệ mới với thiết kế mỏng nhẹ thời thượng."
                ),
                new SeedPhone(
                        "Vivo V30 Pro",
                        "Vivo",
                        599,
                        "/images/vivo-v30-pro.jpg",
                        "Sự kết hợp hoàn hảo với ZEISS đem lại chất ảnh chuyên nghiệp trong phân khúc."
                ),
                new SeedPhone(
                        "iPhone 15 Pro",
                        "Apple",
                        1099,
                        "/images/iphone-15-pro.jpg",
                        "Khung viền Titanium siêu nhẹ, hiệu năng đỉnh cao nằm gọn trong lòng bàn tay."
                ),
                new SeedPhone(
                        "Samsung Galaxy A55 5G",
                        "Samsung",
                        449,
                        "/images/samsung-a55.jpg",
                        "Vua tầm trung của Samsung với khung viền kim loại sang trọng và bảo mật Knox."
                ),
                new SeedPhone(
                        "Xiaomi Redmi Note 13 Pro+",
                        "Xiaomi",
                        399,
                        "/images/xiaomi-redmi-note-13-pro-plus.jpg",
                        "Màn hình cong cao cấp, camera 200MP siêu nét và sạc siêu tốc 120W."
                ),
                new SeedPhone(
                        "OnePlus 12R",
                        "OnePlus",
                        499,
                        "/images/oneplus-12r.jpg",
                        "Phiên bản rút gọn hoàn hảo của OnePlus 12 với dung lượng pin cực khủng."
                ),
                new SeedPhone(
                        "Samsung Galaxy Z Flip5",
                        "Samsung",
                        999,
                        "/images/samsung-z-flip5.jpg",
                        "Điện thoại gập vỏ sò thời trang với màn hình phụ Flex Window lớn độc đáo."
                ),
                new SeedPhone(
                        "iPhone 14 Pro Max",
                        "Apple",
                        1049,
                        "/images/iphone-14-pro-max.jpg",
                        "Sức mạnh bền bỉ với chip A16 Bionic và màn hình Dynamic Island thế hệ đầu."
                )
        );
    }

    private record SeedPhone(
            String name,
            String brand,
            long usdPrice,
            String image,
            String description
    ) {
    }
}
