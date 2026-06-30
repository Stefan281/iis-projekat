package com.iis.backend.service;

import com.iis.backend.dto.CostAnalysisResponseDto;
import com.iis.backend.dto.CostAnalysisSummaryDto;
import com.iis.backend.dto.MonthlyCostDto;
import com.iis.backend.dto.TripCostItemDto;
import com.iis.backend.model.Accommodation;
import com.iis.backend.model.Transport;
import com.iis.backend.model.Trip;
import com.iis.backend.model.TripParticipant;
import com.iis.backend.repository.TripRepository;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CostAnalysisService {

    private final TripRepository tripRepository;

    public CostAnalysisService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<TripCostItemDto> getTripCostItems(int year) {
        List<Trip> matching = new ArrayList<Trip>();
        List<Trip> trips = tripRepository.findAll();
        for (Trip trip : trips) {
            LocalDate departure = trip.getDepartureDate();
            if (departure == null) {
                continue;
            }
            if (departure.getYear() != year) {
                continue;
            }
            matching.add(trip);
        }
        sortByDeparture(matching);

        List<TripCostItemDto> result = new ArrayList<TripCostItemDto>();
        for (Trip trip : matching) {
            result.add(buildItem(trip));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TripCostItemDto> getTripCostItemsForMonth(int year, int month) {
        List<Trip> matching = new ArrayList<Trip>();
        List<Trip> trips = tripRepository.findAll();
        for (Trip trip : trips) {
            LocalDate departure = trip.getDepartureDate();
            if (departure == null) {
                continue;
            }
            if (departure.getYear() != year) {
                continue;
            }
            if (departure.getMonthValue() != month) {
                continue;
            }
            matching.add(trip);
        }
        sortByDeparture(matching);

        List<TripCostItemDto> result = new ArrayList<TripCostItemDto>();
        for (Trip trip : matching) {
            result.add(buildItem(trip));
        }
        return result;
    }

    private void sortByDeparture(List<Trip> trips) {
        for (int i = 0; i < trips.size(); i++) {
            int minIndex = i;
            for (int j = i + 1; j < trips.size(); j++) {
                LocalDate candidate = trips.get(j).getDepartureDate();
                LocalDate current = trips.get(minIndex).getDepartureDate();
                if (candidate.isBefore(current)) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                Trip temp = trips.get(i);
                trips.set(i, trips.get(minIndex));
                trips.set(minIndex, temp);
            }
        }
    }

    @Transactional(readOnly = true)
    public CostAnalysisSummaryDto getSummary(int year) {
        List<TripCostItemDto> items = getTripCostItems(year);
        return buildBaseSummary(items, year);
    }

    @Transactional(readOnly = true)
    public CostAnalysisSummaryDto getMonthlySummary(int year, int month) {
        List<TripCostItemDto> items = getTripCostItemsForMonth(year, month);
        return buildBaseSummary(items, year);
    }

    @Transactional(readOnly = true)
    public List<MonthlyCostDto> getMonthlyBreakdown(int year) {
        double[] totals = new double[12];
        int[] counts = new int[12];
        for (int i = 0; i < 12; i++) {
            totals[i] = 0;
            counts[i] = 0;
        }

        List<Trip> trips = tripRepository.findAll();
        for (Trip trip : trips) {
            LocalDate departure = trip.getDepartureDate();
            if (departure == null) {
                continue;
            }
            if (departure.getYear() != year) {
                continue;
            }
            int index = departure.getMonthValue() - 1;
            TripCostItemDto item = buildItem(trip);
            totals[index] = totals[index] + item.getTotalCost();
            counts[index] = counts[index] + 1;
        }

        List<MonthlyCostDto> breakdown = new ArrayList<MonthlyCostDto>();
        for (int i = 0; i < 12; i++) {
            MonthlyCostDto dto = new MonthlyCostDto();
            dto.setMonthLabel(monthName(i + 1));
            dto.setTotalCost(totals[i]);
            dto.setTripCount(counts[i]);
            breakdown.add(dto);
        }
        return breakdown;
    }

    @Transactional(readOnly = true)
    public CostAnalysisResponseDto getCostAnalysis(int year, int month) {
        CostAnalysisResponseDto response = new CostAnalysisResponseDto();
        if (month <= 0) {
            response.setSummary(getSummary(year));
            response.setTrips(getTripCostItems(year));
        } else {
            response.setSummary(getMonthlySummary(year, month));
            response.setTrips(getTripCostItemsForMonth(year, month));
        }
        response.setMonthlyBreakdown(getMonthlyBreakdown(year));
        return response;
    }

    @Transactional(readOnly = true)
    public byte[] generateReport(int year, int month) {
        if (month <= 0) {
            List<TripCostItemDto> items = getTripCostItems(year);
            CostAnalysisSummaryDto summary = getSummary(year);
            String subtitle = "Godišnji izveštaj - " + year;
            return writePdf(subtitle, summary, items);
        }
        List<TripCostItemDto> items = getTripCostItemsForMonth(year, month);
        CostAnalysisSummaryDto summary = getMonthlySummary(year, month);
        String subtitle = "Mesečni izveštaj - " + monthName(month) + " " + year;
        return writePdf(subtitle, summary, items);
    }

    public String buildReportFileName(int year, int month) {
        if (month <= 0) {
            return "Izvestaj-" + year + ".pdf";
        }
        return "Izvestaj-" + monthName(month) + "-" + year + ".pdf";
    }

    private byte[] writePdf(String subtitle, CostAnalysisSummaryDto summary,
                            List<TripCostItemDto> items) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", 16);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, "Cp1250", 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1250", 10);

        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, output);
            document.open();

            document.add(new Paragraph("Izveštaj o troškovima putovanja - OK Vojvodina", titleFont));
            document.add(new Paragraph(subtitle, subtitleFont));
            document.add(new Paragraph("Datum generisanja: " + formatDate(LocalDate.now()), normalFont));
            document.add(new Paragraph(" ", normalFont));

            document.add(new Paragraph("Ukupan broj putovanja: " + summary.getTripCount(), normalFont));
            document.add(new Paragraph("Ukupna potrošnja: " + formatMoney(summary.getTotalSpent()), normalFont));
            document.add(new Paragraph("Prosečni trošak po putovanju: " + formatMoney(summary.getAverageCostPerTrip()), normalFont));
            document.add(new Paragraph(" ", normalFont));

            Table table = new Table(6);
            table.setWidth(100);
            table.setPadding(4);
            table.addCell(new Cell(new Phrase("Naziv", headerFont)));
            table.addCell(new Cell(new Phrase("Destinacija", headerFont)));
            table.addCell(new Cell(new Phrase("Datumi", headerFont)));
            table.addCell(new Cell(new Phrase("Smeštaj", headerFont)));
            table.addCell(new Cell(new Phrase("Prevoz", headerFont)));
            table.addCell(new Cell(new Phrase("Ukupno", headerFont)));

            for (TripCostItemDto item : items) {
                table.addCell(new Cell(new Phrase(item.getTripName(), normalFont)));
                table.addCell(new Cell(new Phrase(item.getDestination(), normalFont)));
                table.addCell(new Cell(new Phrase(formatDateRange(item), normalFont)));
                table.addCell(new Cell(new Phrase(item.getAccommodationName() + " - " + formatMoney(item.getAccommodationCost()), normalFont)));
                table.addCell(new Cell(new Phrase(item.getTransportName() + " - " + formatMoney(item.getTransportCost()), normalFont)));
                table.addCell(new Cell(new Phrase(formatMoney(item.getTotalCost()), normalFont)));
            }

            document.add(table);
            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Greška pri generisanju PDF izveštaja", ex);
        }
        return output.toByteArray();
    }

    private CostAnalysisSummaryDto buildBaseSummary(List<TripCostItemDto> items, int year) {
        double totalSpent = 0;
        for (TripCostItemDto item : items) {
            totalSpent = totalSpent + item.getTotalCost();
        }

        int tripCount = items.size();

        double average = 0;
        if (tripCount > 0) {
            average = totalSpent / tripCount;
        }

        CostAnalysisSummaryDto summary = new CostAnalysisSummaryDto();
        summary.setTotalSpent(totalSpent);
        summary.setTripCount(tripCount);
        summary.setAverageCostPerTrip(average);
        summary.setYear(year);
        return summary;
    }

    private TripCostItemDto buildItem(Trip trip) {
        TripCostItemDto item = new TripCostItemDto();
        item.setTripId(trip.getId());
        item.setTripName(trip.getName());
        item.setDestination(trip.getLocation());
        item.setDepartureDateFormatted(formatDate(trip.getDepartureDate()));

        if (trip.getReturnDate() != null) {
            item.setReturnDateFormatted(formatDate(trip.getReturnDate()));
        } else {
            item.setReturnDateFormatted("");
        }

        String accommodationName = "Nema";
        double accommodationCost = 0;
        List<Accommodation> accommodationOptions = trip.getAccommodationOptions();
        if (accommodationOptions != null) {
            for (Accommodation accommodation : accommodationOptions) {
                if (accommodation.isSelected()) {
                    accommodationName = accommodation.getName();
                    if (accommodation.getPrice() != null) {
                        accommodationCost = accommodation.getPrice().doubleValue();
                    }
                    break;
                }
            }
        }
        item.setAccommodationName(accommodationName);
        item.setAccommodationCost(accommodationCost);

        String transportName = "Nema";
        double transportCost = 0;
        List<Transport> transportOptions = trip.getTransportOptions();
        if (transportOptions != null) {
            for (Transport transport : transportOptions) {
                if (transport.isSelected()) {
                    transportName = transport.getCarrierName();
                    if (transport.getPrice() != null) {
                        transportCost = transport.getPrice().doubleValue();
                    }
                    break;
                }
            }
        }
        item.setTransportName(transportName);
        item.setTransportCost(transportCost);

        item.setTotalCost(accommodationCost + transportCost);


        return item;
    }

    private String formatDateRange(TripCostItemDto item) {
        if (item.getReturnDateFormatted() == null || item.getReturnDateFormatted().isEmpty()) {
            return item.getDepartureDateFormatted();
        }
        return item.getDepartureDateFormatted() + " - " + item.getReturnDateFormatted();
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear()+".";
    }

    private String formatMoney(double value) {
        long rounded = Math.round(value);
        return rounded + " €";
    }

    private String monthName(int month) {
        if (month == 1) {
            return "Januar";
        }
        if (month == 2) {
            return "Februar";
        }
        if (month == 3) {
            return "Mart";
        }
        if (month == 4) {
            return "April";
        }
        if (month == 5) {
            return "Maj";
        }
        if (month == 6) {
            return "Jun";
        }
        if (month == 7) {
            return "Jul";
        }
        if (month == 8) {
            return "Avgust";
        }
        if (month == 9) {
            return "Septembar";
        }
        if (month == 10) {
            return "Oktobar";
        }
        if (month == 11) {
            return "Novembar";
        }
        return "Decembar";
    }
}
