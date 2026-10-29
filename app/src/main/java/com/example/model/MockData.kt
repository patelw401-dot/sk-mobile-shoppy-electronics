package com.example.model

object MockData {

    val products = listOf(
        // Mobile Phones (New)
        Product(
            id = "mob-1",
            name = "OnePlus Nord CE4 (8GB RAM, 128GB)",
            brand = "OnePlus",
            category = ProductCategory.MOBILE_PHONES,
            price = 24999,
            originalPrice = 27999,
            rating = 4.7f,
            reviewCount = 342,
            description = "Powered by Snapdragon 7 Gen 3 with 100W SUPERVOOC fast charging and 5500 mAh battery. Vibrant 120Hz Fluid AMOLED display with Sony LYT-600 OIS camera sensor.",
            specifications = mapOf(
                "Processor" to "Qualcomm Snapdragon 7 Gen 3 (4nm)",
                "RAM / Storage" to "8GB LPDDR4X / 128GB UFS 3.1",
                "Display" to "6.7\" FHD+ 120Hz Fluid AMOLED",
                "Rear Camera" to "50MP Sony LYT-600 (OIS) + 8MP Ultra-wide",
                "Front Camera" to "16MP HDR Selfie",
                "Battery & Charging" to "5500 mAh, 100W SUPERVOOC",
                "OS" to "OxygenOS 14 based on Android 14",
                "Warranty" to "1 Year Brand Warranty in India"
            ),
            isFeatured = true,
            highlightBadge = "Best Seller"
        ),
        Product(
            id = "mob-2",
            name = "Samsung Galaxy M35 5G (6GB, 128GB)",
            brand = "Samsung",
            category = ProductCategory.MOBILE_PHONES,
            price = 16999,
            originalPrice = 21499,
            rating = 4.5f,
            reviewCount = 289,
            description = "Monster 6000 mAh battery with Knox Security and Samsung Wallet support. Stunning 120Hz Super AMOLED display and 50MP No Shake Camera.",
            specifications = mapOf(
                "Processor" to "Exynos 1380 Octa-core",
                "RAM / Storage" to "6GB RAM / 128GB Expandable",
                "Display" to "6.6\" 120Hz Super AMOLED (1000 nits)",
                "Camera" to "50MP (OIS) + 8MP + 2MP",
                "Battery" to "6000 mAh Mega Battery, 25W Fast Charge",
                "Security" to "Samsung Knox Vault, Fingerprint Sensor"
            ),
            isFeatured = true,
            highlightBadge = "Hot Deal"
        ),
        Product(
            id = "mob-3",
            name = "Redmi Note 13 Pro 5G (8GB, 256GB)",
            brand = "Xiaomi",
            category = ProductCategory.MOBILE_PHONES,
            price = 23999,
            originalPrice = 28999,
            rating = 4.6f,
            reviewCount = 412,
            description = "Flagship 200MP OIS camera with 1.5K 120Hz curved display, Snapdragon 7s Gen 2, and 67W turbo charging. Corning Gorilla Glass Victus protection.",
            specifications = mapOf(
                "Processor" to "Snapdragon 7s Gen 2 (4nm)",
                "Display" to "6.67\" 1.5K AMOLED 120Hz Dolby Vision",
                "Rear Camera" to "200MP Samsung ISOCELL HP3 (OIS) + 8MP + 2MP",
                "Front Camera" to "16MP Ultra-clear Selfie",
                "Battery" to "5100 mAh, 67W Turbo Charge In-box"
            ),
            isFeatured = true
        ),
        Product(
            id = "mob-4",
            name = "Realme 12+ 5G (8GB RAM, 128GB)",
            brand = "Realme",
            category = ProductCategory.MOBILE_PHONES,
            price = 18999,
            originalPrice = 22999,
            rating = 4.4f,
            reviewCount = 195,
            description = "Luxury watch dial design with premium vegan leather finish. Sony LYT-600 OIS camera, 67W SUPERVOOC charging, and MediaTek Dimensity 7050 5G chipset.",
            specifications = mapOf(
                "Processor" to "MediaTek Dimensity 7050 5G",
                "Display" to "6.67\" 120Hz AMOLED 2000 nits Peak",
                "Camera" to "50MP Sony LYT-600 OIS + 8MP Wide + 2MP Macro",
                "Battery" to "5000 mAh, 67W Fast Charging"
            )
        ),

        // Old Phones (Pre-Owned / Certified Refurbished)
        Product(
            id = "old-1",
            name = "Apple iPhone 12 (128GB) - Certified Pre-Owned",
            brand = "Apple",
            category = ProductCategory.OLD_PHONES,
            price = 28499,
            originalPrice = 49900,
            rating = 4.8f,
            reviewCount = 142,
            description = "Thoroughly inspected with SK Mobile Shoppy and Electronic 32-point quality check. 100% genuine components with pristine Super Retina XDR OLED display, A14 Bionic chip, and verified battery health.",
            specifications = mapOf(
                "Condition" to "Grade A+ (Like New, zero scratches)",
                "Battery Health" to "92% Original Apple Battery",
                "Screen" to "6.1\" Super Retina XDR OLED True Tone",
                "Chipset" to "Apple A14 Bionic 6-core",
                "Included" to "Original Type-C Cable + SK Mobile Shoppy and Electronic 6-Month Warranty Card",
                "Inspection" to "32-Point Technician Passed (SK Certified)"
            ),
            isPreOwned = true,
            conditionGrade = "Grade A+ (Like New)",
            batteryHealth = "92%",
            warranty = "6 Months SK Mobile Shoppy and Electronic Replacement Warranty",
            isFeatured = true,
            highlightBadge = "Certified Refurbished"
        ),
        Product(
            id = "old-2",
            name = "OnePlus 9 Pro 5G (12GB, 256GB) - Pre-Owned",
            brand = "OnePlus",
            category = ProductCategory.OLD_PHONES,
            price = 21999,
            originalPrice = 64999,
            rating = 4.6f,
            reviewCount = 88,
            description = "Hasselblad quad camera flagship with curved QHD+ 120Hz LTPO display. Completely sanitized, tested, and backed by SK Mobile Shoppy and Electronic warranty in Udgir.",
            specifications = mapOf(
                "Condition" to "Grade A (Minor cosmetic hairline, 100% functional)",
                "Battery Health" to "89% Tested Battery",
                "Display" to "6.7\" QHD+ 120Hz Fluid AMOLED LTPO",
                "Processor" to "Snapdragon 888 5G",
                "Warranty" to "6 Months Warranty at SK Mobile Shoppy and Electronic Udgir"
            ),
            isPreOwned = true,
            conditionGrade = "Grade A (Clean)",
            batteryHealth = "89%",
            warranty = "6 Months SK Mobile Shoppy and Electronic Shop Warranty"
        ),
        Product(
            id = "old-3",
            name = "Samsung Galaxy S21 FE 5G (8GB, 128GB)",
            brand = "Samsung",
            category = ProductCategory.OLD_PHONES,
            price = 19499,
            originalPrice = 49999,
            rating = 4.5f,
            reviewCount = 67,
            description = "Pro-grade triple camera with 30X Space Zoom, Snapdragon 888, and Dynamic AMOLED 2X 120Hz display. Screen and battery certified by local technicians.",
            specifications = mapOf(
                "Condition" to "Grade A (Super Clean)",
                "Battery Health" to "91%",
                "Display" to "6.4\" FHD+ Dynamic AMOLED 2X 120Hz",
                "Camera" to "12MP OIS + 12MP Ultra-wide + 8MP 3X Telephoto",
                "Warranty" to "6 Months Local Shop Warranty"
            ),
            isPreOwned = true,
            conditionGrade = "Grade A (Like New)",
            batteryHealth = "91%",
            warranty = "6 Months Warranty"
        ),

        // Chargers
        Product(
            id = "chg-1",
            name = "SK SpeedCharge 65W GaN Dual Port Adapter (Type-C + USB-A)",
            brand = "SK Mobile",
            category = ProductCategory.CHARGERS,
            price = 1299,
            originalPrice = 2499,
            rating = 4.8f,
            reviewCount = 512,
            description = "Next-gen Gallium Nitride (GaN) technology for ultra-compact 65W power. Supports VOOC, Warp, QC 4.0, PD 3.0, and PPS. Charges laptops, tablets, and phones safely.",
            specifications = mapOf(
                "Output Power" to "65W Max Power Delivery",
                "Technology" to "GaN III High Efficiency Cooling",
                "Ports" to "1x Type-C (65W) + 1x USB-A (18W)",
                "Protection" to "Over-voltage, Over-heating, Short-circuit cut-off",
                "Compatibility" to "OnePlus, Samsung, iPhone, Vivo, Xiaomi, MacBooks"
            ),
            isFeatured = true,
            highlightBadge = "Top Rated"
        ),
        Product(
            id = "chg-2",
            name = "Samsung 25W Type-C Super Fast Wall Adapter (Original)",
            brand = "Samsung",
            category = ProductCategory.CHARGERS,
            price = 999,
            originalPrice = 1699,
            rating = 4.7f,
            reviewCount = 378,
            description = "Genuine 25W Power Delivery (PD) 3.0 adapter with PPS support. Designed specifically for Galaxy S-series, A-series, and M-series smartphones.",
            specifications = mapOf(
                "Output" to "25W Max (PD 3.0 PPS)",
                "Port" to "USB Type-C",
                "Voltage" to "100-240V Universal Input",
                "Warranty" to "6 Months Brand Warranty"
            )
        ),
        Product(
            id = "chg-3",
            name = "Portronics Adapto 33W Dual Output Fast Charger",
            brand = "Portronics",
            category = ProductCategory.CHARGERS,
            price = 699,
            originalPrice = 1499,
            rating = 4.5f,
            reviewCount = 188,
            description = "Compact 33W fast wall adapter with Mach USB and Type-C Power Delivery. Certified by BIS for Indian electrical outlets.",
            specifications = mapOf(
                "Output" to "33W Max Output",
                "Ports" to "USB-A + Type-C PD",
                "Certification" to "BIS Indian Standard Certified"
            )
        ),

        // Data Cables
        Product(
            id = "cbl-1",
            name = "SK UltraBraid 100W Type-C to Type-C Fast Charging Cable (1.5m)",
            brand = "SK Mobile",
            category = ProductCategory.DATA_CABLES,
            price = 349,
            originalPrice = 799,
            rating = 4.9f,
            reviewCount = 680,
            description = "Military-grade ballistic nylon braided cable with zinc-alloy connectors. E-Marker smart chip supports up to 100W PD charging and 480Mbps data sync.",
            specifications = mapOf(
                "Power Rating" to "100W (20V / 5A) Max PD",
                "Cable Length" to "1.5 Meters (Extra Long)",
                "Material" to "Double Braided Nylon + Aluminum Shell",
                "Bend Lifespan" to "30,000+ Bends Tested",
                "Data Speed" to "480 Mbps High Speed Sync"
            ),
            isFeatured = true,
            highlightBadge = "Customer Favorite"
        ),
        Product(
            id = "cbl-2",
            name = "boAt Rugged v3 Extra Tough Type-C Cable (1.2m)",
            brand = "boAt",
            category = ProductCategory.DATA_CABLES,
            price = 249,
            originalPrice = 599,
            rating = 4.6f,
            reviewCount = 420,
            description = "High-tensile stress resistant cable with tangle-free braided outer sheath. Supports 3A fast charging for all Android smartphones.",
            specifications = mapOf(
                "Rating" to "3A Rapid Charging",
                "Length" to "1.2m",
                "Connector" to "USB-A to Type-C"
            )
        ),
        Product(
            id = "cbl-3",
            name = "SK Premium Braided Lightning to Type-C Cable for iPhone (1.2m)",
            brand = "SK Mobile",
            category = ProductCategory.DATA_CABLES,
            price = 499,
            originalPrice = 1199,
            rating = 4.7f,
            reviewCount = 210,
            description = "C94 Apple compatible chip for 27W high speed charging for iPhone 11/12/13/14 and iPads. Reinforced stress joints prevent fraying.",
            specifications = mapOf(
                "Compatibility" to "iPhone 8 through 14 Pro Max & iPad",
                "Charging" to "27W PD Rapid Boost",
                "Length" to "1.2m Braided"
            )
        ),

        // Headphones
        Product(
            id = "hdp-1",
            name = "Sony WH-CH520 Wireless On-Ear Bluetooth Headphones",
            brand = "Sony",
            category = ProductCategory.HEADPHONES,
            price = 3990,
            originalPrice = 4990,
            rating = 4.7f,
            reviewCount = 312,
            description = "Up to 50 hours of massive battery life with DSEE sound enhancement engine. Lightweight swivel design with multi-point Bluetooth pairing.",
            specifications = mapOf(
                "Battery Life" to "50 Hours Playback (3 min charge = 1.5 hrs)",
                "Sound Tech" to "Sony DSEE Sound Engine + 360 Reality Audio",
                "Microphone" to "Built-in Clear Voice HD Mic",
                "Connectivity" to "Bluetooth 5.2 Multipoint Connection",
                "Weight" to "147g Featherweight Comfort"
            ),
            isFeatured = true
        ),
        Product(
            id = "hdp-2",
            name = "boAt Rockerz 450 Pro Wireless Bluetooth Headphone",
            brand = "boAt",
            category = ProductCategory.HEADPHONES,
            price = 1499,
            originalPrice = 3990,
            rating = 4.5f,
            reviewCount = 560,
            description = "40mm dynamic drivers with boAt Signature Sound and punchy bass. 70 hours non-stop playtime with ASAP fast charging.",
            specifications = mapOf(
                "Drivers" to "40mm Dynamic Bass Drivers",
                "Playtime" to "70 Hours Battery Playback",
                "Fast Charge" to "10 min charge = 10 hours playback",
                "Design" to "Foldable with Plush Memory Foam Cushions"
            ),
            highlightBadge = "Hot Deal"
        ),

        // Bluetooth Devices
        Product(
            id = "bt-1",
            name = "OnePlus Nord Buds 2 TWS Earbuds with Active Noise Cancellation",
            brand = "OnePlus",
            category = ProductCategory.BLUETOOTH,
            price = 2699,
            originalPrice = 3299,
            rating = 4.6f,
            reviewCount = 445,
            description = "25dB Active Noise Cancellation with BassWave algorithm. 12.4mm extra large titanium dynamic drivers and 36 hours total battery.",
            specifications = mapOf(
                "ANC" to "25dB Active Noise Cancellation",
                "Drivers" to "12.4mm Titanium Dynamic Drivers",
                "Total Playtime" to "36 Hours with Charging Case",
                "Water Resistance" to "IP55 Dust and Water Resistant",
                "Latency" to "94ms Ultra-low Game Mode"
            ),
            isFeatured = true,
            highlightBadge = "Trending"
        ),
        Product(
            id = "bt-2",
            name = "Noise Buds VS102 Plus Wireless Earbuds (70H Playtime)",
            brand = "Noise",
            category = ProductCategory.BLUETOOTH,
            price = 1199,
            originalPrice = 2999,
            rating = 4.4f,
            reviewCount = 380,
            description = "Instacharge fast charging, 11mm speaker drivers, Quad Mic with Environmental Noise Cancellation (ENC) for crystal clear calling.",
            specifications = mapOf(
                "Playtime" to "70 Hours Total Playback",
                "Calling" to "Quad Mic with ENC Noise Cancellation",
                "Water Resistance" to "IPX5 Splash Proof"
            )
        ),
        Product(
            id = "bt-3",
            name = "JBL GO 3 Ultra-Portable Waterproof Bluetooth Speaker",
            brand = "JBL",
            category = ProductCategory.BLUETOOTH,
            price = 2999,
            originalPrice = 3999,
            rating = 4.8f,
            reviewCount = 520,
            description = "Iconic JBL Pro Sound in a rugged, pocket-friendly design. IP67 waterproof and dustproof with integrated carry loop.",
            specifications = mapOf(
                "Sound" to "JBL Pro Signature Bass",
                "Battery" to "5 Hours Continuous Playtime",
                "Durability" to "IP67 Fully Waterproof & Dustproof",
                "Bluetooth" to "Bluetooth v5.1"
            )
        ),

        // Accessories
        Product(
            id = "acc-1",
            name = "SK PowerVault 20000mAh 22.5W Fast Charging Power Bank",
            brand = "SK Mobile",
            category = ProductCategory.ACCESSORIES,
            price = 1499,
            originalPrice = 2999,
            rating = 4.8f,
            reviewCount = 390,
            description = "High-density Lithium Polymer battery capable of charging 3 devices simultaneously. Digital LED battery percentage display and two-way fast charge.",
            specifications = mapOf(
                "Capacity" to "20000 mAh High-Density Li-Po",
                "Max Output" to "22.5W QC 3.0 / PD Fast Charging",
                "Ports" to "2x USB-A Output + 1x Type-C In/Out",
                "Display" to "Digital Percentage LED Screen",
                "Safety" to "12-Layer Circuit Protection Chip"
            ),
            isFeatured = true,
            highlightBadge = "Must Have"
        ),
        Product(
            id = "acc-2",
            name = "9D Edge-to-Edge Full Glue Tempered Glass (Professional Installation in Udgir)",
            brand = "SK Mobile",
            category = ProductCategory.ACCESSORIES,
            price = 199,
            originalPrice = 499,
            rating = 4.9f,
            reviewCount = 920,
            description = "9H hardness shatterproof glass with oleophobic anti-fingerprint coating and curved black borders. Free professional bubble-free installation at SK Mobile Shoppy and Electronic Udgir shop.",
            specifications = mapOf(
                "Hardness" to "9H Scratch Resistant Japanese Asahi Glass",
                "Coating" to "Nano Oleophobic Anti-smudge Layer",
                "Clarity" to "99.9% High Definition Transparency",
                "Installation" to "Free Bubble-free Fitting at Store"
            )
        ),
        Product(
            id = "acc-3",
            name = "Heavy Duty 360° Rotating Car Dashboard Mobile Holder",
            brand = "Portronics",
            category = ProductCategory.ACCESSORIES,
            price = 399,
            originalPrice = 899,
            rating = 4.6f,
            reviewCount = 215,
            description = "Industrial strength suction gel pad that locks onto car dashboards or windshields. One-touch release button with telescopic extendable arm.",
            specifications = mapOf(
                "Mount Type" to "Dashboard / Windshield Strong Gel Suction",
                "Rotation" to "360-Degree Ball Joint Swivel",
                "Phone Width" to "Fits phones from 4.0\" to 7.0\""
            )
        )
    )

    val repairServices = listOf(
        RepairService(
            id = "rep-1",
            title = "Display Replacement",
            estimatedStartingPrice = 1499,
            turnaroundTime = "30 - 60 Minutes",
            warrantyDays = 90,
            description = "Cracked glass, blank screen, lines, or touch responsiveness issues. Original OLED/IPS screens with genuine touch calibration and 90-day warranty.",
            iconType = "display"
        ),
        RepairService(
            id = "rep-2",
            title = "Battery Replacement",
            estimatedStartingPrice = 699,
            turnaroundTime = "20 - 45 Minutes",
            warrantyDays = 180,
            description = "Quick battery drain, overheating, phone swelling, or unexpected shutdowns. Grade-A certified OEM batteries with 6-month replacement warranty.",
            iconType = "battery"
        ),
        RepairService(
            id = "rep-3",
            title = "Charging Problem",
            estimatedStartingPrice = 349,
            turnaroundTime = "30 Minutes",
            warrantyDays = 90,
            description = "Loose charging jack, slow charging, moisture detected, or complete charging failure. Precision Type-C / Micro-USB port replacement.",
            iconType = "charging"
        ),
        RepairService(
            id = "rep-4",
            title = "Speaker / Mic Problem",
            estimatedStartingPrice = 299,
            turnaroundTime = "30 - 45 Minutes",
            warrantyDays = 90,
            description = "Crackling audio, low call volume, receiver issues, or caller cannot hear your voice. Original ear piece and bottom speaker replacement.",
            iconType = "audio"
        ),
        RepairService(
            id = "rep-5",
            title = "Software Problem & Flashing",
            estimatedStartingPrice = 249,
            turnaroundTime = "45 - 90 Minutes",
            warrantyDays = 30,
            description = "Phone hanging, stuck on logo (boot loop), FRP / pattern unlock, OS update failures, or network unlocking. Done safely without bricking risk.",
            iconType = "software"
        ),
        RepairService(
            id = "rep-6",
            title = "Water Damage Recovery",
            estimatedStartingPrice = 599,
            turnaroundTime = "2 - 4 Hours",
            warrantyDays = 60,
            description = "Ultrasonic motherboard cleaning, chemical corrosion removal, and short circuit diagnosis with specialized thermal camera inspection.",
            iconType = "water"
        )
    )

    val customerReviews = listOf(
        CustomerReview(
            id = "rev-1",
            customerName = "Sachin Patil",
            location = "Nagar Parishad Road, Udgir",
            rating = 5,
            date = "Yesterday",
            comment = "My OnePlus display was completely shattered. SK Mobile Shoppy and Electronic technicians replaced it in just 45 minutes with genuine display at a fraction of the showroom price! Honest people, highly recommended in Udgir.",
            serviceOrProduct = "Display Replacement"
        ),
        CustomerReview(
            id = "rev-2",
            customerName = "Ganesh Kulkarni",
            location = "Shivaji Chowk, Udgir",
            rating = 5,
            date = "3 days ago",
            comment = "Bought an iPhone 12 certified pre-owned from SK Mobile Shoppy and Electronic. The condition is like brand new with 92% battery health, bill, and warranty. Best place in Udgir to buy second hand phones.",
            serviceOrProduct = "iPhone 12 (Certified Old Phone)"
        ),
        CustomerReview(
            id = "rev-3",
            customerName = "Pooja Shinde",
            location = "Bidar Road, Udgir",
            rating = 5,
            date = "1 week ago",
            comment = "Booked home pickup for battery replacement because I couldn't visit the market. The boy arrived on time, picked up the phone, and delivered it back perfectly working by evening. Great service!",
            serviceOrProduct = "Home Pickup Repair"
        ),
        CustomerReview(
            id = "rev-4",
            customerName = "Imran Shaikh",
            location = "Nanded Naka, Udgir",
            rating = 5,
            date = "2 weeks ago",
            comment = "Got the 65W GaN charger and braided cable for my phone. Fast charging works brilliantly without overheating. Authentic mobile accessories store in Udgir.",
            serviceOrProduct = "65W Fast Charger & Cable"
        )
    )

    val popularBrands = listOf("Apple", "Samsung", "OnePlus", "Xiaomi", "Realme", "Vivo", "Oppo", "Motorola", "boAt", "Noise")

    val defaultReels = listOf(
        ReelItem(
            id = "reel-1",
            title = "OnePlus Nord CE4 5G Unboxing & 100W Charging Test!",
            caption = "⚡ Watch the 100W SUPERVOOC blast from 1% to 100% in under 30 minutes! Available in store now with official brand warranty & instant store discounts at SK Mobile Shoppy & Electronic Udgir.",
            videoUrl = "raw://videoplayback",
            thumbnailUrl = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80",
            productId = "mob-1",
            productName = "OnePlus Nord CE4 (8GB RAM, 128GB)",
            productPrice = 24999,
            productImage = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&q=80",
            likesCount = 142,
            sharesCount = 38,
            viewsCount = 1250,
            isPublished = true,
            uploadedAt = System.currentTimeMillis() - 86400000L * 2,
            durationSeconds = 25
        ),
        ReelItem(
            id = "reel-2",
            title = "Samsung Galaxy M35 5G - Monster 6000mAh Battery!",
            caption = "🔋 Heavy gaming & non-stop YouTube streaming! The 6000mAh battery handles it with ease. Knox security + 120Hz Super AMOLED screen. Come check it out in store!",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80",
            productId = "mob-2",
            productName = "Samsung Galaxy M35 5G (6GB, 128GB)",
            productPrice = 16999,
            productImage = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80",
            likesCount = 210,
            sharesCount = 54,
            viewsCount = 1890,
            isPublished = true,
            uploadedAt = System.currentTimeMillis() - 86400000L * 3,
            durationSeconds = 15
        ),
        ReelItem(
            id = "reel-3",
            title = "iPhone 13 / 14 Display Replacement in 30 Minutes!",
            caption = "🛠️ Broken or flickering screen? Our certified repair engineers in Udgir replace authentic OEM displays with 90-day warranty. Doorstep pickup & delivery available!",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1563770660941-20978e870e26?w=800&q=80",
            productId = null,
            productName = null,
            productPrice = null,
            productImage = null,
            likesCount = 328,
            sharesCount = 89,
            viewsCount = 3400,
            isPublished = true,
            uploadedAt = System.currentTimeMillis() - 86400000L * 5,
            durationSeconds = 15
        ),
        ReelItem(
            id = "reel-4",
            title = "GaN 65W Fast Charger - Charge 3 Devices At Once!",
            caption = "🚀 Laptop, Tablet, and Smartphone all charging super fast from a single pocket-sized adapter. Smart heat protection. Special discount price this week only!",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=800&q=80",
            productId = "chg-1",
            productName = "65W GaN Triple Port Fast Charger",
            productPrice = 1999,
            productImage = "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=800&q=80",
            likesCount = 95,
            sharesCount = 21,
            viewsCount = 820,
            isPublished = true,
            uploadedAt = System.currentTimeMillis() - 86400000L * 7,
            durationSeconds = 20
        )
    )
}
