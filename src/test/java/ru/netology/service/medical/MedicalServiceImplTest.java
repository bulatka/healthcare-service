package ru.netology.service.medical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

public class MedicalServiceImplTest {

    static final private String ID = "123";
    static final private String NAME = "Bulat";
    static final private String SURNAME = "Sabitov";
    static private final LocalDate BIRTHDAY =
            LocalDate.of(1988, 1, 15);
    static private final HealthInfo HEALTH_INFO_WARNING =
            new HealthInfo(BigDecimal.valueOf(38.8),
                    new BloodPressure(200, 100));
    static private final HealthInfo HEALTH_INFO_NORMAL =
            new HealthInfo(BigDecimal.valueOf(36.6),
                    new BloodPressure(150, 100));
    private static final BigDecimal WARNING_TEMPERATURE = BigDecimal.valueOf(37.2);

    final private PatientInfoFileRepository PIFR =
            Mockito.mock(PatientInfoFileRepository.class);
    final private SendAlertService SAS = Mockito.mock(SendAlertService.class);

    static final private PatientInfo PI_WARNING = new PatientInfo(
            ID, NAME, SURNAME, BIRTHDAY, HEALTH_INFO_WARNING);
    static final private PatientInfo PI_NORMAL = new PatientInfo(
            ID, NAME, SURNAME, BIRTHDAY, HEALTH_INFO_NORMAL);
    final private BloodPressure WARNING_BLOOD_PRESSURE =
            new BloodPressure(150, 100);
    static final private String MESSAGE =
            String.format("Warning, patient with id: %s, need help", ID);
    static final private String NULL = null;

    @Test
    void testCheckBloodPressure() {
        MedicalService medicalService = new MedicalServiceImpl(PIFR, SAS);
        Mockito.when(PIFR.getById(Mockito.anyString())).thenReturn(PI_WARNING);

        medicalService.checkBloodPressure(PI_WARNING.getId(), WARNING_BLOOD_PRESSURE);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(SAS).send(argumentCaptor.capture());

        Assertions.assertEquals(MESSAGE, argumentCaptor.getValue());
    }

    @Test
    void testCheckTemperature() {
        MedicalService medicalService = new MedicalServiceImpl(PIFR, SAS);
        Mockito.when(PIFR.getById(Mockito.anyString())).thenReturn(PI_WARNING);

        medicalService.checkTemperature(ID, WARNING_TEMPERATURE);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(SAS).send(argumentCaptor.capture());

        Assertions.assertEquals(MESSAGE, argumentCaptor.getValue());
    }

    @ParameterizedTest
    @MethodSource("methodSource")
    void testCheckBloodPressure(PatientInfo PI, int times) {
        MedicalService medicalService = new MedicalServiceImpl(PIFR, SAS);
        Mockito.when(PIFR.getById(Mockito.anyString())).thenReturn(PI);

        medicalService.checkBloodPressure(PI.getId(), WARNING_BLOOD_PRESSURE);

        Mockito.verify(SAS, times(times)).send(any());
    }

    @ParameterizedTest
    @MethodSource("methodSource")
    void testCheckTemperature(PatientInfo PI, int times) {
        MedicalService medicalService = new MedicalServiceImpl(PIFR, SAS);
        Mockito.when(PIFR.getById(Mockito.anyString())).thenReturn(PI);

        medicalService.checkTemperature(PI.getId(), WARNING_TEMPERATURE);

        Mockito.verify(SAS, times(times)).send(any());
    }

    public static Stream<Arguments> methodSource() {
        return Stream.of(
                Arguments.of(PI_WARNING, 1),
                Arguments.of(PI_NORMAL, 0)
        );
    }
}