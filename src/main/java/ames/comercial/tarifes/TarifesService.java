package ames.comercial.tarifes;

import ames.comercial.shared.ValidationResult;
import ames.comercial.tarifes.beans.Preu;
import ames.comercial.tarifes.beans.Tarifa;
import ames.comercial.tarifes.beans.validation.TarifaValidator;
import ames.comercial.tarifes.internal.domain.DadesTarifa;
import ames.comercial.tarifes.internal.domain.DadesTarifaImpl;
import ames.comercial.tarifes.internal.infraestructure.TarifaRepository;
import ames.comercial.tarifes.internal.infraestructure.mapper.PreuRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TarifesService {

    TarifaRepository tarifaRepo;

    static final Logger log = LogManager.getLogger(TarifesService.class.getName());

    @Autowired
    TarifaXLSConfig config;

    public TarifesService(TarifaRepository tarifaRepo) {
        this.tarifaRepo = tarifaRepo;
    }

    public void validate(Tarifa tarifa) {

        TarifaValidator tarifaValidator = new TarifaValidator();

        ValidationResult validationResult = tarifa.getValidationResult();

        //TODO validar que hi ha preu de tot el cataleg de peçes actual. S'haura de fer una consulta a BBDD
        // Es podria fer usant un hash SHA256

        // Spring validations
        Set<ConstraintViolation<Tarifa>> violations = tarifaValidator.validate(tarifa);
        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            for (ConstraintViolation<Tarifa> violation : violations) {
                errors.append(violation.getMessage()).append("\n");
            }
            validationResult.invalidate(errors.toString());
        }

        if (validationResult.isValid()) {
            // Custom validations
            boolean customValidationFailed = true;
            List<Preu> preus = tarifa.getPreus();
            if (preus.size() == 0)
                validationResult.invalidate("No hi ha preus a processar");
            int iPreu = 3;
            for (Preu preu : preus) {
                // Correlative prices validation
//                if (preu.getPr12() > preu.getPr11() ||
//                        preu.getPr11() > preu.getPr10() ||
//                        preu.getPr10() > preu.getPr09() ||
//                        preu.getPr09() > preu.getPr08() ||
//                        preu.getPr08() > preu.getPr07() ||
//                        preu.getPr07() > preu.getPr06() ||
//                        preu.getPr06() > preu.getPr05() ||
//                        preu.getPr05() > preu.getPr04() ||
//                        preu.getPr04() > preu.getPr03() ||
//                        preu.getPr03() > preu.getPr02() ||
//                        preu.getPr02() > preu.getPr01()) {
//                    validationResult.invalidate("Preus no correlatius. El preu1 ha de ser major que el preu2 etc... Revisar fila=" + iPreu);
//                    break;
//                }
//                if (preu.getPr01() < 0 ||
//                        preu.getPr02() < 0 ||
//                        preu.getPr03() < 0 ||
//                        preu.getPr04() < 0 ||
//                        preu.getPr05() < 0 ||
//                        preu.getPr06() < 0 ||
//                        preu.getPr07() < 0 ||
//                        preu.getPr08() < 0 ||
//                        preu.getPr09() < 0 ||
//                        preu.getPr10() < 0 ||
//                        preu.getPr11() < 0 ||
//                        preu.getPr12() < 0) {
//                    validationResult.invalidate("Preus negatius. Revisar fila=" + iPreu);
//                    break;
//                }
                iPreu++;
            }
            // Negative quantities

            // Columna tarifa amb el mateix nom de la tarifa que volem donar d'alta o modificar
        }
    }

    List<DadesTarifa> findTarifes(String name, String divisa, Integer any, String estat) {
        return tarifaRepo.findTarifes(name, divisa, any, estat);
    }

    Optional<DadesTarifa> findByNom(String nom) {
        return tarifaRepo.findByNom(nom);
    }

    Optional<DadesTarifa> findByCodi(Long codi) {
        return tarifaRepo.findByCodi(codi);
    }

    boolean exists(String nom) {
        return tarifaRepo.exists(nom);
    }

    boolean exists(Long codi) {
        return tarifaRepo.exists(codi);
    }

    public int tancarTarifa(Long codi, String user) {
        // "status", DadesTarifa.ENUM_STATUS_CLOSED, "updated_by", user,"updated_at", LocalDateTime.now(), "updated_reason", DadesTarifa.ENUM_STATUS_CLOSED)
        // TODO Crida a rutina que actualitzi la relacio del clients anteriors relacionats amb la tarifa vinculada a aquesta.
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put("status", DadesTarifa.ENUM_STATUS_CLOSED);
        fields.put("updated_by", user);
        fields.put("updated_reason", "SYSTEM_TANCO_TARIFA"); // TODO HARDCODED

        return tarifaRepo.updateTarifaFields(codi,
                fields);
    }

//    public int update(Long codi, String nom, Long vinculada, String divisa, String user, String reason) {
//        // "status", DadesTarifa.ENUM_STATUS_CLOSED, "updated_by", user,"updated_at", LocalDateTime.now(), "updated_reason", DadesTarifa.ENUM_STATUS_CLOSED)
//        HashMap<String, Object> fields = new HashMap<String, Object>();
//            fields.put("nom", nom);
//            fields.put("divisa", divisa);
//            fields.put("vinculada", vinculada);
//            fields.put("updated_by", user);
//            fields.put("updated_reason", reason);
//        tarifaRepo.updateTarifaFields(codi, fields);
//        return 1; //TODO revisar el numero de registres actualitzats
//    }

    //TODO refactor, en principi no caldria guardar les dades de capçalera de la tarifa, nomes els preus...
    Tarifa getPreusFromXLSOld(InputStream fis, String nom, String divisa) throws IOException {

        Tarifa tarifa = new Tarifa();
        tarifa.setNom(nom);
        tarifa.setDivisa(divisa);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Preu preu = null;
        List<Preu> preus = new ArrayList<Preu>();
        ;

        Sheet sheet = workbook.getSheetAt(0);
        int iRow = 1;
        int iCell;
        for (Row row : sheet) {
            iCell = 1;
            if (iRow < 3) {// (1,2) header
                if (iRow != 2 && row.getLastCellNum() != Integer.parseInt(config.getNumofcolumns())) {
                    ValidationResult validationResult = new ValidationResult();
                    validationResult.invalidate("Numero de columnes incorrecte: " + row.getLastCellNum());
                    tarifa.setValidationResult(validationResult);
                    log.error("Numero de columnes incorrecte: " + row.getLastCellNum());
                    break;
                }
                if (iRow == 2) {
                    for (int col = Tarifa.i_tram1; col <= Tarifa.i_tram12; col++) {
                        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        Integer valorNumericICalculat = null;
                        if (cell.getCellType() == CellType.FORMULA)
                            valorNumericICalculat =  getEvaluatedCellValueAsDouble(formulaEvaluator.evaluate(cell)).intValue();
                        if (cell.getCellType() == CellType.NUMERIC)
                            valorNumericICalculat = (int) cell.getNumericCellValue();
                        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().equals(""))
                            valorNumericICalculat = 999999;
                        if (cell.getCellType() == CellType.BLANK && cell.getStringCellValue().trim().equals(""))
                            valorNumericICalculat = 999999;
                        switch (col) {
                            case Tarifa.i_tram1:
                                tarifa.setTram1(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram2:
                                tarifa.setTram2(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram3:
                                tarifa.setTram3(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram4:
                                tarifa.setTram4(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram5:
                                tarifa.setTram5(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram6:
                                tarifa.setTram6(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram7:
                                tarifa.setTram7(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram8:
                                tarifa.setTram8(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram9:
                                tarifa.setTram9(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram10:
                                tarifa.setTram10(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram11:
                                tarifa.setTram11(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram12:
                                tarifa.setTram12(valorNumericICalculat);
                                break;
                        }
                    }
                }
            } else {
                iCell = 1;
                preu = new Preu();
                for (Cell cell : row) {
//                    if (iCell==1 && (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().equals("")))
//                        break;
//                    System.out.print("Coord[" + iRow + "," + iCell + "]");
                    if (iCell >= Preu.i_artInt && iCell <= Preu.i_aclRef) {
                        switch (iCell) {
                            case Preu.i_artInt:
                                preu.setArtInt(cell.getStringCellValue());
                                break;
                            case Preu.i_aclFab:
                                preu.setAclFab(cell.getStringCellValue());
                                break;
                            case Preu.i_aclRef:
                                preu.setAclRef(cell.getStringCellValue());
                                break;

                        }
//                        System.out.print(cell.getStringCellValue()+" ");
                    }
                    if (iCell >= Preu.i_pr01 && iCell <= Preu.i_unsCai) {
                        Double valorNumericICalculat = null;
                        if (cell.getCellType() == CellType.FORMULA)
                            valorNumericICalculat = getEvaluatedCellValueAsDouble(formulaEvaluator.evaluate(cell));
                        if (cell.getCellType() == CellType.NUMERIC)
                            valorNumericICalculat = cell.getNumericCellValue();

                        switch (iCell) {
                            case Preu.i_pr01:
                                preu.setPr01(valorNumericICalculat);
                                break;
                            case Preu.i_pr02:
                                preu.setPr02(valorNumericICalculat);
                                break;
                            case Preu.i_pr03:
                                preu.setPr03(valorNumericICalculat);
                                break;
                            case Preu.i_pr04:
                                preu.setPr04(valorNumericICalculat);
                                break;
                            case Preu.i_pr05:
                                preu.setPr05(valorNumericICalculat);
                                break;
                            case Preu.i_pr06:
                                preu.setPr06(valorNumericICalculat);
                                break;
                            case Preu.i_pr07:
                                preu.setPr07(valorNumericICalculat);
                                break;
                            case Preu.i_pr08:
                                preu.setPr08(valorNumericICalculat);
                                break;
                            case Preu.i_pr09:
                                preu.setPr09(valorNumericICalculat);
                                break;
                            case Preu.i_pr10:
                                preu.setPr10(valorNumericICalculat);
                                break;
                            case Preu.i_pr11:
                                preu.setPr11(valorNumericICalculat);
                                break;
                            case Preu.i_pr12:
                                preu.setPr12(valorNumericICalculat);
                                break;
//                            case Preu.i_unsBos:
//                                preu.setUnsBos(valorNumericICalculat.intValue());
//                                break;
//                            case Preu.i_unsCai:
//                                preu.setUnsCai(valorNumericICalculat.intValue());
//                                break;
                            default:
                                break;
                        }
//                        System.out.print(valorNumericICalculat+" ");
                    }
                    iCell++;
                }
//                System.out.println();
                preus.add(preu);
            }
            iRow++;
        }
        tarifa.setPreus(preus);

        validate(tarifa);

        return tarifa;
    }

    Tarifa getPreusFromXLS(InputStream fis) throws IOException {

        Tarifa tarifa = new Tarifa();
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

        tarifa.setNom(workbook.getSheetAt(0).getRow(2).getCell(1).getStringCellValue());
        tarifa.setDivisa(workbook.getSheetAt(0).getRow(2).getCell(2).getStringCellValue());

        if (exists(tarifa.getNom())) {
            ValidationResult validationResult = new ValidationResult();
            validationResult.invalidate("Nom duplicat de tarifa: " + tarifa.getNom());
            tarifa.setValidationResult(validationResult);
            return tarifa;
        }

        Preu preu = null;
        List<Preu> preus = new ArrayList<Preu>();

        Sheet sheet = workbook.getSheetAt(0);
        int iRow = 1;
        int iCell;
        for (Row row : sheet) {
            iCell = 1;
            if (iRow < 3) {// (1,2) header
                if (iRow != 2 && row.getLastCellNum() != Integer.parseInt(config.getNumofcolumns())) {
                    ValidationResult validationResult = new ValidationResult();
                    validationResult.invalidate("Numero de columnes incorrecte: " + row.getLastCellNum());
                    tarifa.setValidationResult(validationResult);
                    log.error("Numero de columnes incorrecte: " + row.getLastCellNum());
                    break;
                }
                if (iRow == 2) {
                    for (int col = Tarifa.i_tram1; col <= Tarifa.i_tram12; col++) {
                        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        Integer valorNumericICalculat = null;
                        if (cell.getCellType() == CellType.FORMULA)
                            valorNumericICalculat =  getEvaluatedCellValueAsDouble(formulaEvaluator.evaluate(cell)).intValue();
                        if (cell.getCellType() == CellType.NUMERIC)
                            valorNumericICalculat = (int) cell.getNumericCellValue();
                        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().equals(""))
                            valorNumericICalculat = 999999;
                        if (cell.getCellType() == CellType.BLANK && cell.getStringCellValue().trim().equals(""))
                            valorNumericICalculat = 999999;
                        switch (col) {
                            case Tarifa.i_tram1:
                                tarifa.setTram1(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram2:
                                tarifa.setTram2(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram3:
                                tarifa.setTram3(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram4:
                                tarifa.setTram4(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram5:
                                tarifa.setTram5(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram6:
                                tarifa.setTram6(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram7:
                                tarifa.setTram7(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram8:
                                tarifa.setTram8(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram9:
                                tarifa.setTram9(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram10:
                                tarifa.setTram10(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram11:
                                tarifa.setTram11(valorNumericICalculat);
                                break;
                            case Tarifa.i_tram12:
                                tarifa.setTram12(valorNumericICalculat);
                                break;
                        }
                    }
                }
            } else {
                iCell = 1;
                preu = new Preu();
                for (Cell cell : row) {
//                    if (iCell==1 && (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().equals("")))
//                        break;
                    System.out.print("Coord[" + iRow + "," + iCell + "]");
                    if (iCell >= Preu.i_artInt && iCell <= Preu.i_aclRef) {
                        switch (iCell) {
                            case Preu.i_artInt:
                                preu.setArtInt(cell.getStringCellValue());
                                break;
                            case Preu.i_aclFab:
                                preu.setAclFab(cell.getStringCellValue());
                                break;
                            case Preu.i_aclRef:
                                preu.setAclRef(cell.getStringCellValue());
                                break;

                        }
//                        System.out.print(cell.getStringCellValue()+" ");
                    }
                    if (iCell >= Preu.i_pr01 && iCell <= Preu.i_unsCai) {
                        Double valorNumericICalculat = null;
                        if (cell.getCellType() == CellType.FORMULA)
                            valorNumericICalculat = getEvaluatedCellValueAsDouble(formulaEvaluator.evaluate(cell));
                        if (cell.getCellType() == CellType.NUMERIC)
                            valorNumericICalculat = cell.getNumericCellValue();

                        switch (iCell) {
                            case Preu.i_pr01:
                                preu.setPr01(valorNumericICalculat);
                                break;
                            case Preu.i_pr02:
                                preu.setPr02(valorNumericICalculat);
                                break;
                            case Preu.i_pr03:
                                preu.setPr03(valorNumericICalculat);
                                break;
                            case Preu.i_pr04:
                                preu.setPr04(valorNumericICalculat);
                                break;
                            case Preu.i_pr05:
                                preu.setPr05(valorNumericICalculat);
                                break;
                            case Preu.i_pr06:
                                preu.setPr06(valorNumericICalculat);
                                break;
                            case Preu.i_pr07:
                                preu.setPr07(valorNumericICalculat);
                                break;
                            case Preu.i_pr08:
                                preu.setPr08(valorNumericICalculat);
                                break;
                            case Preu.i_pr09:
                                preu.setPr09(valorNumericICalculat);
                                break;
                            case Preu.i_pr10:
                                preu.setPr10(valorNumericICalculat);
                                break;
                            case Preu.i_pr11:
                                preu.setPr11(valorNumericICalculat);
                                break;
                            case Preu.i_pr12:
                                preu.setPr12(valorNumericICalculat);
                                break;
//                            case Preu.i_unsBos:
//                                preu.setUnsBos(valorNumericICalculat.intValue());
//                                break;
//                            case Preu.i_unsCai:
//                                preu.setUnsCai(valorNumericICalculat.intValue());
//                                break;
                            default:
                                break;
                        }
//                        System.out.print(valorNumericICalculat+" ");
                    }
                    iCell++;
                }
//                System.out.println();
                preus.add(preu);
            }
            iRow++;
        }
        tarifa.setPreus(preus);
//
        validate(tarifa);

        return tarifa;
    }


    private Double getEvaluatedCellValueAsDouble(CellValue cellValue) {
        switch (cellValue.getCellType()) {
            case STRING:
                return Double.valueOf(cellValue.getStringValue());
            case NUMERIC:
                return cellValue.getNumberValue();
            default:
                return null;
        }
    }

    @Transactional
    public void newTarifaOld(Tarifa tarifa, String user, Long vinculada) {
//            if (validationResult.isValid() || validationResult.processable()) {
        DadesTarifaImpl.Builder builder = DadesTarifaImpl.builder()
                .nom(tarifa.getNom())
                .divisa(tarifa.getDivisa())
                .tram01(tarifa.getTram1())
                .tram02(tarifa.getTram2())
                .tram03(tarifa.getTram3())
                .tram04(tarifa.getTram4())
                .tram05(tarifa.getTram5())
                .tram06(tarifa.getTram6())
                .tram07(tarifa.getTram7())
                .tram08(tarifa.getTram8())
                .tram09(tarifa.getTram9())
                .tram10(tarifa.getTram10())
                .tram11(tarifa.getTram11())
                .tram12(tarifa.getTram12())
                .insertedAt(LocalDateTime.now())
                .insertedBy(user)
//                    .updatedAt(null)
//                    .updatedBy(null)
                .enabled(false)
                .deleted(false)
                .status(DadesTarifa.ENUM_STATUS_DRAFT);
        if (vinculada != null)
            builder.vinculada(vinculada);
        Long codiTarifa = tarifaRepo.save(builder.build());
        tarifaRepo.savePreus(codiTarifa, tarifa.getPreus());
    }

    @Transactional
    public void newTarifa(Tarifa tarifa, String user) {
//            if (validationResult.isValid() || validationResult.processable()) {
        DadesTarifaImpl.Builder builder = DadesTarifaImpl.builder()
                .nom(tarifa.getNom())
                .divisa(tarifa.getDivisa())
                .tram01(tarifa.getTram1())
                .tram02(tarifa.getTram2())
                .tram03(tarifa.getTram3())
                .tram04(tarifa.getTram4())
                .tram05(tarifa.getTram5())
                .tram06(tarifa.getTram6())
                .tram07(tarifa.getTram7())
                .tram08(tarifa.getTram8())
                .tram09(tarifa.getTram9())
                .tram10(tarifa.getTram10())
                .tram11(tarifa.getTram11())
                .tram12(tarifa.getTram12())
                .insertedAt(LocalDateTime.now())
                .insertedBy(user)
//                    .updatedAt(null)
//                    .updatedBy(null)
                .enabled(false)
                .deleted(false)
                .status(DadesTarifa.ENUM_STATUS_DRAFT);

        Long codiTarifa = tarifaRepo.save(builder.build());
        tarifaRepo.savePreus(codiTarifa, tarifa.getPreus());
    }

    public void updateTarifa(Long codi, String nom, String divisa, Long vinculada, String user, String reason) {
        updateTarifa(codi, nom, divisa, vinculada, user, reason, null);
    }

    @Transactional
    public void updateTarifa(Long codi, String nom, String divisa, Long vinculada, String user, String reason, List<Preu> preus) {
        //Si divisa esta informat llavors esborro la vinculació, i al revés
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put("nom", nom);
        fields.put("divisa", divisa);
        fields.put("vinculada", vinculada);
        fields.put("updated_by", user);
        fields.put("updated_reason", reason);
        tarifaRepo.updateTarifaFields(codi, fields);
        if (preus != null) {
            deletePreus(codi);
            tarifaRepo.savePreus(codi, preus);
        }
    }

    public List<Preu> findPreus(Long codi_tarifa) {
        return tarifaRepo.findPreus(codi_tarifa);
    }

    public List<PreuRecord> findPreusRecord(Long codi_tarifa) {
        return tarifaRepo.findPreusRecord(codi_tarifa);
    }

    public StreamingOutput downloadXLS(DadesTarifa tarifa, List<Preu> preus) throws IOException {
        // TODO Cal fer una crida a algun proces (ARTICLES?) per que ens doni les daes PESUN i PES BASE

        String tarifaNom = tarifa.nom();
        String divisa = tarifa.divisa();

        // Cargar la plantilla de Excel desde el classpath
        InputStream templateInputStream = new ClassPathResource("template_tarifa.xlsx").getInputStream();
        Workbook workbook = new XSSFWorkbook(templateInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Row rowCap2Trams = sheet.createRow(1);

        for (int colNum = 6; colNum < 18; colNum++) {
            Cell cell = rowCap2Trams.createCell(colNum);
            switch (colNum) {
                case 6:
                    cell.setCellValue(tarifa.tram01());
                    break;
                case 7:
                    cell.setCellValue(tarifa.tram02());
                    break;
                case 8:
                    cell.setCellValue(tarifa.tram03());
                    break;
                case 9:
                    cell.setCellValue(tarifa.tram04());
                    break;
                case 10:
                    cell.setCellValue(tarifa.tram05());
                    break;
                case 11:
                    cell.setCellValue(tarifa.tram06());
                    break;
                case 12:
                    cell.setCellValue(tarifa.tram07());
                    break;
                case 13:
                    cell.setCellValue(tarifa.tram08());
                    break;
                case 14:
                    cell.setCellValue(tarifa.tram09());
                    break;
                case 15:
                    cell.setCellValue(tarifa.tram10());
                    break;
                case 16:
                    cell.setCellValue(tarifa.tram11());
                    break;
                case 17:
                    cell.setCellValue(tarifa.tram12());
                    break;
            }

        }

        // Escribir datos en la hoja
        int rowNum = 2; // Asumiendo que la fila 0 tiene 2 encabezados
        for (Preu rowPreu : preus) {
            Row row = sheet.createRow(rowNum++);
            for (int colNum = 0; colNum < 22; colNum++) {
                Cell cell = row.createCell(colNum);
                switch (colNum) {
                    case 0:
                        cell.setCellValue(String.format("%04d", rowNum - 2));
                        break;
                    case 1:
                        cell.setCellValue(tarifaNom);
                        break;
                    case 2:
                        cell.setCellValue(divisa);
                        break;
                    case 3:
                        cell.setCellValue(rowPreu.getArtInt());
                        break;
                    case 4:
                        cell.setCellValue(rowPreu.getAclFab());
                        break;
                    case 5:
                        cell.setCellValue(rowPreu.getAclRef());
                        break;
//                    case 6:
//                        cell.setCellValue("??");
//                        break;
//                    case 7:
//                        cell.setCellValue("??");
//                        break;
                    case 6:
                        cell.setCellValue(rowPreu.getPr01());
                        break;
                    case 7:
                        cell.setCellValue(rowPreu.getPr02());
                        break;
                    case 8:
                        cell.setCellValue(rowPreu.getPr03());
                        break;
                    case 9:
                        cell.setCellValue(rowPreu.getPr04());
                        break;
                    case 10:
                        cell.setCellValue(rowPreu.getPr05());
                        break;
                    case 11:
                        cell.setCellValue(rowPreu.getPr06());
                        break;
                    case 12:
                        cell.setCellValue(rowPreu.getPr07());
                        break;
                    case 13:
                        cell.setCellValue(rowPreu.getPr08());
                        break;
                    case 14:
                        cell.setCellValue(rowPreu.getPr09());
                        break;
                    case 15:
                        cell.setCellValue(rowPreu.getPr10());
                        break;
                    case 16:
                        cell.setCellValue(rowPreu.getPr11());
                        break;
                    case 17:
                        cell.setCellValue(rowPreu.getPr12());
                        break;
//                    case 20:
//                        cell.setCellValue(rowPreu.getUnsBos());
//                        break;
//                    case 21:
//                        cell.setCellValue(rowPreu.getUnsCai());
//                        break;
                }

            }
        }

        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream out)
                    throws IOException, WebApplicationException {

                workbook.write(out);
                out.flush();
            }
        };

        // Devolver el archivo modificado como ByteArrayResource
        return output;
    }

    public int delete(Long codi, String user) {
// BAIXA LOGICA        return tarifaRepo.updateTarifaFields(tarifa, Map.of("deleted", true, "deleted_by", user,"deleted_at",LocalDateTime.now(), "deleted_reason", reason));
//        Optional<DadesTarifa> dadesTarifa = tarifaRepo.find(tarifa);
        tarifaRepo.delete(codi);
        // Borra tots els preu en per politica de CASCADA
        //TODO return number of deleted fields
        return 1;
    }

    public int deletePreus(Long codi_tarifa) {
        return tarifaRepo.deletePreus(codi_tarifa);
    }
}
