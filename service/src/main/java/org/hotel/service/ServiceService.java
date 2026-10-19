package org.hotel.service;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hotel.model.constants.CommandConstants;
import org.hotel.model.constants.ParametersConstants;
import org.hotel.model.entities.Service;
import org.hotel.model.enums.ServiceSection;
import org.hotel.model.exceptions.ServiceAlreadyExistsException;
import org.hotel.model.exceptions.ServiceNotFoundException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.dao.repository.ServiceRepository;
import org.hotel.model.enums.SortType;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class ServiceService {
    /**
     * Логгер.
     */
    private static final Logger LOGGER = LogManager.getLogger(ServiceService.class);

    /**
     * Репозиторий для работы с услугами в БД.
     */
    private final ServiceRepository serviceRepository;

    public ServiceService(final ServiceRepository serviceRepositoryP) {
        this.serviceRepository = serviceRepositoryP;
    }

    public void addNewService(final Service serviceP) {
        if (isThereService(serviceP.getId())) {
            throw new ServiceAlreadyExistsException();
        }
        serviceRepository.save(serviceP);
    }

    public Service getService(final String idP) {
        if (!isThereService(idP)) {
            throw new ServiceNotFoundException();
        }
        return serviceRepository.getService(idP);
    }

    public String getServiceName(final String idP) {
        if (!isThereService(idP)) {
            throw new ServiceNotFoundException();
        }
        return getService(idP).getName();
    }

    public BigDecimal getServicePrice(final String idP) {
        if (!isThereService(idP)) {
            throw new ServiceNotFoundException();
        }
        return getService(idP).getPrice();
    }

    public List<Service> getServices() {
        return serviceRepository.findAll();
    }

    public void setNewServicePrice(final String idP, final BigDecimal newPriceP) {
        if (!isThereService(idP)) {
            throw new ServiceNotFoundException();
        }
        if (newPriceP.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException();
        }
        serviceRepository.setNewServicePrice(getService(idP), newPriceP);
    }

    public List<Service> getServicesWithSort(final SortType sortTypeP) {
        if (sortTypeP != SortType.PRICE && sortTypeP != SortType.SECTION) {
            throw new WrongSortTypeException();
        }
        List<Service> listServices = getServices();
        if (sortTypeP == SortType.PRICE) {
            listServices.sort(Comparator.comparing(Service::getPrice));
        } else if (sortTypeP == SortType.SECTION) {
            listServices.sort(Comparator.comparing(Service::getServiceSection));
        }
        return listServices;
    }

    public boolean isThereService(final String idP) {
        return serviceRepository.getService(idP) != null;
    }

    public String importServices(MultipartFile multipartFile) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
            String line;
            int successCount = 0;
            int errorCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == ParametersConstants.SERVICE_PARAMETERS_COUNT) {
                    try {
                        Service service = new Service(parts[CommandConstants.COMMAND_ZERO], parts[CommandConstants.COMMAND_ONE], new BigDecimal(parts[CommandConstants.COMMAND_TWO]), ServiceSection.valueOf(parts[CommandConstants.COMMAND_THREE]));
                        addNewService(service);
                        successCount++;
                    } catch (Exception e) {
                        LOGGER.error("Ошибка обработки строки " + line + ": " + e.getMessage());
                        errorCount++;
                    }
                } else {
                    LOGGER.error("Ошибка при импорте. Неверное количество параметров");
                    errorCount++;
                }
            }
            return "Импорт завершен. Количество ошибок: " + errorCount + ", количество успешно считанных строк: " + successCount;
        }
    }

    public String exportServices() {
        List<Service> services = getServices();
        StringBuilder stringBuilder = new StringBuilder();
        for (Service service : services) {
            String[] data = new String[ParametersConstants.SERVICE_PARAMETERS_COUNT];
            data[CommandConstants.COMMAND_ZERO] = service.getId();
            data[CommandConstants.COMMAND_ONE] = service.getName();
            data[CommandConstants.COMMAND_TWO] = String.valueOf(service.getPrice());
            data[CommandConstants.COMMAND_THREE] = service.getServiceSection().name();
            String resultLine = String.join(";", data);
            stringBuilder.append(resultLine).append("\n");
        }
        return stringBuilder.toString();
    }
}
