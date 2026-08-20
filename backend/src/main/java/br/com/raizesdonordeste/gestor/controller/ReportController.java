package br.com.raizesdonordeste.gestor.controller;

import br.com.raizesdonordeste.gestor.dto.SalesByUnitDto;
import br.com.raizesdonordeste.gestor.dto.TopProductDto;
import br.com.raizesdonordeste.gestor.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales-by-unit")
    public List<SalesByUnitDto> salesByUnit() {
        return reportService.salesByUnit();
    }

    @GetMapping("/top-products")
    public List<TopProductDto> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return reportService.topProducts(limit);
    }
}
