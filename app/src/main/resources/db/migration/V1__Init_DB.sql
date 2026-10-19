CREATE TABLE rooms (
    roomId VARCHAR(36) PRIMARY KEY,
    number INT NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    stars INT CHECK (stars BETWEEN 1 AND 5),
    status VARCHAR(20) NOT NULL,
    releasedIn DATE
);

CREATE TABLE guests (
    guestId VARCHAR(36) PRIMARY KEY,
    fullName VARCHAR(128),
    age INT,
    rentRoomId VARCHAR(36),
    arriveDate DATE,
    departureDate DATE,
    pricePerDay DECIMAL(10,2),
    status VARCHAR(36) CHECK (status IN ('SETTLED', 'EVICTED')),
    FOREIGN KEY (rentRoomId) REFERENCES rooms(roomId)
);

CREATE TABLE services (
    serviceId VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    serviceSection VARCHAR(50)
);

CREATE TABLE guestUsedServices (
    usedServiceId BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    guestId VARCHAR(36) NOT NULL,
    serviceId VARCHAR(36) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    date DATE NOT NULL,
    FOREIGN KEY (guestId) REFERENCES guests(guestId),
    FOREIGN KEY (serviceId) REFERENCES services(serviceId)
);