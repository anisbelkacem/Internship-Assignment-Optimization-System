package com.aspd.backend.service;

import com.aspd.backend.common.exception.NotFoundException;
import com.aspd.backend.dto.SchoolImportResult;
import com.aspd.backend.dto.SchoolRequest;
import com.aspd.backend.model.School;
import com.aspd.backend.model.SchoolType;
import com.aspd.backend.repository.SchoolRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
public class SchoolService {
    private final SchoolRepository repository;

    public SchoolService(SchoolRepository repository) {
        this.repository = repository;
    }

    public List<School> list() {
        return repository.findAll();
    }

    public School get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("School not found: " + id));
    }

    @Transactional
    public School create(SchoolRequest req) {
        School s = new School();
        apply(s, req);
        return repository.save(s);
    }

    @Transactional
    public School update(Long id, SchoolRequest req) {
        School s = get(id);
        apply(s, req);
        return repository.save(s);
    }

    @Transactional
    public void delete(Long id) {
        School s = get(id);
        repository.delete(s);
    }

    private void apply(School s, SchoolRequest req) {
        s.setName(req.getName());
        s.setAddress(req.getAddress());
        s.setZone(req.getZone());
        s.setOepnv(req.getOepnv());
        s.setType(req.getType());
    }

    @Transactional
    public SchoolImportResult importFromExcel(InputStream inputStream) {
        SchoolImportResult result = new SchoolImportResult();
        List<School> toSave = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("No sheets found in the Excel file");
            }

            if (sheet.getPhysicalNumberOfRows() <= 1) {
                result.setTotalRows(0);
                return result;
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(sheet.getRow(0));
            validateRequiredHeaders(headerIndex, result);
            if (result.getErrorCount() > 0) return result;

            int total = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                total++;
                try {
                    School s = parseRow(row, headerIndex);
                    toSave.add(s);
                } catch (IllegalArgumentException e) {
                    result.addError("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
            result.setTotalRows(total);

            if (!toSave.isEmpty()) {
                repository.saveAll(toSave);
                result.setImportedCount(toSave.size());
            }
            result.setErrorCount(result.getErrors().size());
            return result;
        } catch (IllegalArgumentException e) {
            result.addError(e.getMessage());
            result.setErrorCount(result.getErrors().size());
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read Excel file: " + e.getMessage());
        }
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        Map<String, Integer> idx = new HashMap<>();
        if (headerRow == null) return idx;
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell == null) continue;
            String key = normalize(headerString(cell));
            if (!key.isEmpty()) idx.put(key, c);
        }
        return idx;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerIndex, SchoolImportResult result) {
        List<String> required = List.of("name", "address", "zone", "oepnv", "type");
        for (String r : required) {
            if (!headerIndex.containsKey(r)) {
                result.addError("Missing required column: " + r);
            }
        }
        result.setErrorCount(result.getErrors().size());
    }

    private School parseRow(Row row, Map<String, Integer> headerIndex) {
        String name = readString(row, headerIndex.get("name"));
        String address = readString(row, headerIndex.get("address"));
        String zone = readString(row, headerIndex.get("zone"));
        Boolean oepnv = readBoolean(row, headerIndex.get("oepnv"));
        SchoolType type = readType(row, headerIndex.get("type"));

        if (isBlank(name) || isBlank(address) || isBlank(zone) || oepnv == null || type == null) {
            throw new IllegalArgumentException("Required fields missing or invalid");
        }

        School s = new School();
        s.setName(name);
        s.setAddress(address);
        s.setZone(zone);
        s.setOepnv(oepnv);
        s.setType(type);
        return s;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT);
        t = t.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        t = t.replaceAll("[^a-z0-9]", "");
        return t;
    }

    private String headerString(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private String readString(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            String v = cell.getStringCellValue();
            return isBlank(v) ? null : v.trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double d = cell.getNumericCellValue();
            if (Double.isNaN(d)) return null;
            long l = (long) d;
            if (Math.abs(d - l) < 1e-9) return String.valueOf(l);
            return String.valueOf(d);
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return null;
    }


    private Boolean readBoolean(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String v = cell.getStringCellValue();
            if (isBlank(v)) return null;
            String t = v.trim().toLowerCase(Locale.ROOT);
            if (t.equals("true") || t.equals("yes") || t.equals("ja") || t.equals("1")) return true;
            if (t.equals("false") || t.equals("no") || t.equals("nein") || t.equals("0")) return false;
            throw new IllegalArgumentException("oepnv must be boolean (true/false/yes/no/ja/nein/1/0)");
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return Math.abs(cell.getNumericCellValue()) >= 0.5;
        }
        return null;
    }

    private SchoolType readType(Row row, Integer colIdx) {
        String val = readString(row, colIdx);
        if (isBlank(val)) return null;
        String u = val.trim().toUpperCase(Locale.ROOT);
        if (u.equals("GS")) return SchoolType.GS;
        if (u.equals("MS")) return SchoolType.MS;
        throw new IllegalArgumentException("type must be GS or MS");
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
