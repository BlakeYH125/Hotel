package org.hotel.service;

import org.hotel.model.entities.Service;
import org.hotel.model.enums.ServiceSection;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.ServiceAlreadyExistsException;
import org.hotel.model.exceptions.ServiceNotFoundException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.dao.repository.ServiceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    void addNewService_SaveNewService_WhenAllCorrect() {
        String serviceId = "123";
        Service service = new Service();
        service.setId(serviceId);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(null);

        serviceService.addNewService(service);

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId);
        Mockito.verify(serviceRepository, Mockito.times(1)).save(service);
    }

    @Test
    void addNewService_ThrowServiceAlreadyExistsException_WhenServiceAlreadyExists() {
        String serviceId = "123";
        Service service = new Service();
        service.setId(serviceId);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(service);

        Assertions.assertThrows(ServiceAlreadyExistsException.class, () -> serviceService.addNewService(service));

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId);
    }

    @Test
    void getService_ReturnService_WhenAllCorrect() {
        String serviceId = "123";
        Service service = new Service();
        service.setId(serviceId);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(service);

        Service actual = serviceService.getService(serviceId);

        Assertions.assertEquals(service, actual);

        Mockito.verify(serviceRepository, Mockito.times(2)).getService(serviceId);
    }

    @Test
    void getService_ThrowServiceNotFoundException_WhenServiceNotFound() {
        String serviceId = "123";
        Service service = new Service();
        service.setId(serviceId);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(null);

        Assertions.assertThrows(ServiceNotFoundException.class, () -> serviceService.getService(serviceId));

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId);
    }

    @Test
    void getServiceName_ReturnString_WhenAllCorrect() {
        String serviceId = "123";
        String expectedName = "ужин";
        Service service = new Service();
        service.setId(serviceId);
        service.setName(expectedName);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(service);

        String actual = serviceService.getServiceName(serviceId);

        Assertions.assertEquals(expectedName, actual);

        Mockito.verify(serviceRepository, Mockito.times(3)).getService(serviceId);
    }

    @Test
    void getServiceName_ThrowServiceNotFoundException_WhenServiceNotFound() {
        String serviceId = "123";
        String expectedName = "ужин";
        Service service = new Service();
        service.setId(serviceId);
        service.setName(expectedName);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(null);

        Assertions.assertThrows(ServiceNotFoundException.class, () -> serviceService.getServiceName(serviceId));

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId);
    }

    @Test
    void getServicePrice_ReturnPrice_WhenAllCorrect() {
        String serviceId = "123";
        BigDecimal expectedPrice = new BigDecimal(150);
        Service service = new Service();
        service.setId(serviceId);
        service.setPrice(expectedPrice);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(service);

        BigDecimal actual = serviceService.getServicePrice(serviceId);

        Assertions.assertEquals(expectedPrice, actual);

        Mockito.verify(serviceRepository, Mockito.times(3)).getService(serviceId);
    }

    @Test
    void getServicePrice_ThrowServiceNotFoundException_WhenServiceNotFound() {
        String serviceId = "123";
        BigDecimal expectedPrice = new BigDecimal(150);
        Service service = new Service();
        service.setId(serviceId);
        service.setPrice(expectedPrice);

        Mockito.when(serviceRepository.getService(service.getId())).thenReturn(null);

        Assertions.assertThrows(ServiceNotFoundException.class, () -> serviceService.getServicePrice(serviceId));

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId);
    }

    @Test
    void getServices_ReturnListOfServices() {
        String serviceId1 = "123";
        Service service1 = new Service();
        service1.setId(serviceId1);
        String serviceId2 = "456";
        Service service2 = new Service();
        service2.setId(serviceId2);
        List<Service> expected = new ArrayList<>(List.of(service1, service2));

        Mockito.when(serviceRepository.findAll()).thenReturn(expected);

        List<Service> actual = serviceService.getServices();

        Assertions.assertEquals(expected, actual);

        Mockito.verify(serviceRepository, Mockito.times(1)).findAll();
    }

    @Test
    void setNewServicePrice_ChangeServicePrice_WhenAllCorrect() {
        String serviceId = "123";
        Service service = new Service();
        service.setId(serviceId);

        Mockito.when(serviceRepository.getService(serviceId)).thenReturn(service);

        serviceService.setNewServicePrice(serviceId, new BigDecimal(150));

        Mockito.verify(serviceRepository, Mockito.times(3)).getService(serviceId);
    }

    @Test
    void setNewServicePrice_ThrowServiceNotFoundException_WhenServiceNotFound() {
        String serviceId = "123";
        Service service = new Service();
        service.setId(serviceId);

        Mockito.when(serviceRepository.getService(serviceId)).thenReturn(null);

        Assertions.assertThrows(ServiceNotFoundException.class, () -> serviceService.setNewServicePrice(serviceId, new BigDecimal(150)));

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId);
    }

    @Test
    void setNewServicePrice_ThrowIllegalArgumentException_WhenPriceLessZero() {
        String serviceId = "123";
        Service service = new Service();
        service.setId(serviceId);

        Mockito.when(serviceRepository.getService(serviceId)).thenReturn(service);

        Assertions.assertThrows(IllegalArgumentException.class, () -> serviceService.setNewServicePrice(serviceId, new BigDecimal(-150)));

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId);
    }

    @Test
    void getServicesWithSort_ReturnServicesList_WhenAllCorrect() {
        String serviceId1 = "123";
        Service service1 = new Service();
        service1.setId(serviceId1);
        service1.setPrice(new BigDecimal(500));
        String serviceId2 = "123";
        Service service2 = new Service();
        service2.setId(serviceId2);
        service2.setPrice(new BigDecimal(300));

        List<Service> services = new ArrayList<>(List.of(service2, service1));

        Mockito.when(serviceRepository.findAll()).thenReturn(new ArrayList<>(List.of(service1, service2)));

        List<Service> actual = serviceService.getServicesWithSort(SortType.PRICE);

        Assertions.assertEquals(services, actual);

        Mockito.verify(serviceRepository, Mockito.times(1)).findAll();
    }

    @Test
    void getServicesWithSort_ThrowWrongSortTypeException_WhenWrongSortType() {
        Assertions.assertThrows(WrongSortTypeException.class, () -> serviceService.getServicesWithSort(SortType.ALPHABET));
    }

    @Test
    void isThereService_ReturnTrue_IfServiceExists() {
        String serviceId1 = "123";
        Service service1 = new Service();
        service1.setId(serviceId1);

        Mockito.when(serviceRepository.getService(serviceId1)).thenReturn(service1);

        boolean result = serviceService.isThereService(serviceId1);

        Assertions.assertTrue(result);

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId1);
    }

    @Test
    void isThereService_ReturnFalse_IfServiceDoesNotExists() {
        String serviceId1 = "123";
        Service service1 = new Service();
        service1.setId(serviceId1);

        Mockito.when(serviceRepository.getService(serviceId1)).thenReturn(null);

        boolean result = serviceService.isThereService(serviceId1);

        Assertions.assertFalse(result);

        Mockito.verify(serviceRepository, Mockito.times(1)).getService(serviceId1);
    }

    @Test
    void importService_0Error1Successes_WhenAllCorrect() throws Exception{
        String csv = "s1;ужин;500;FOOD\n";
        MockMultipartFile mockFile = new MockMultipartFile("file", "services.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        Mockito.when(serviceRepository.getService("s1")).thenReturn(null);

        String actual = serviceService.importServices(mockFile);

        String expected = "Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1";

        Assertions.assertEquals(expected, actual);

        Mockito.verify(serviceRepository, Mockito.times(1)).getService("s1");
        Mockito.verify(serviceRepository, Mockito.times(1)).save(ArgumentMatchers.any(Service.class));
    }

    @Test
    void importService_1Error0Successes_WhenWrongNumberOfParameters() throws Exception{
        String csv = "s1;ужин;FOOD\n";

        MockMultipartFile mockFile = new MockMultipartFile("file", "services.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        String actual = serviceService.importServices(mockFile);

        String expected = "Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0";

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void importService_1Error0Successes_WhenServiceAlreadyExists() throws Exception{
        String csv = "s1;ужин;500;FOOD\n";

        Service service = new Service();
        service.setId("s1");

        Mockito.when(serviceRepository.getService("s1")).thenReturn(service);

        MockMultipartFile mockFile = new MockMultipartFile("file", "services.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        String actual = serviceService.importServices(mockFile);

        String expected = "Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0";

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void exportService_ReturnCsvString_WhenAllCorrect() {
        Service service = new Service("s1", "ужин", new BigDecimal(500), ServiceSection.FOOD);

        List<Service> services = new ArrayList<>(List.of(service));

        Mockito.when(serviceRepository.findAll()).thenReturn(services);

        String expected = "s1;ужин;500;FOOD\n";

        String actual = serviceService.exportServices();

        Assertions.assertEquals(expected, actual);

        Mockito.verify(serviceRepository, Mockito.times(1)).findAll();
    }

    @Test
    void exportService_ReturnEmptyLine_WhenDataBaseIsEmpty() {
        Mockito.when(serviceRepository.findAll()).thenReturn(new ArrayList<>());

        String expected = "";

        String actual = serviceService.exportServices();

        Assertions.assertEquals(expected, actual);

        Mockito.verify(serviceRepository, Mockito.times(1)).findAll();
    }
}
