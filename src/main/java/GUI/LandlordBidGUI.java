package GUI;

import UserOperations.IBidManagement;
import UserOperations.IPropertyManagement;
import Data.domain.Bid;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.List;

public class LandlordBidGUI extends JFrame {
    private final IBidManagement bidService;
    private final IPropertyManagement propertyService;
    private final String landlordId;

    public LandlordBidGUI(IBidManagement bidService, IPropertyManagement propertyService, String landlordId) {
        this.bidService = bidService;
        this.propertyService = propertyService;
        this.landlordId = landlordId;

        setTitle("Landlord Dashboard - " + landlordId);
        setSize(900, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();
    }

    private void initializeUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Property Management
        JPanel propertyPanel = new JPanel(new BorderLayout());
        PropertyManagementGUI propertyManagementGUI = new PropertyManagementGUI(landlordId);
        propertyPanel.add(propertyManagementGUI.getContentPane(), BorderLayout.CENTER);
        tabbedPane.addTab("My Properties", propertyPanel);

        // Tab 2: Bid Management
        JPanel bidPanel = createBidPanel();
        tabbedPane.addTab("Property Bids", bidPanel);

        // Tab 3: Bid Reports
        JPanel reportPanel = createReportPanel();
        tabbedPane.addTab("Bid Reports", reportPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Logout Button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new UserOperations().setVisible(true);
            dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createBidPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Bid Display Area
        JTextArea bidArea = new JTextArea();
        bidArea.setEditable(false);
        bidArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(bidArea);

        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Filter Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Filter Status:"), gbc);

        JComboBox<String> filterStatusCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "ACCEPTED", "REJECTED"});
        gbc.gridx = 1;
        controlPanel.add(filterStatusCombo, gbc);

        // Sort Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Sort By:"), gbc);

        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"Date ↑", "Date ↓", "Amount ↑", "Amount ↓"});
        gbc.gridx = 1;
        controlPanel.add(sortCombo, gbc);

        // Refresh Button
        JButton refreshBtn = new JButton("Apply");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        controlPanel.add(refreshBtn, gbc);

        // Bid Selection
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        controlPanel.add(new JLabel("Select Bid:"), gbc);

        JComboBox<Bid> bidCombo = new JComboBox<>();
        bidCombo.setRenderer(new BidListRenderer());
        gbc.gridx = 1;
        controlPanel.add(bidCombo, gbc);

        // Status Update
        gbc.gridx = 0;
        gbc.gridy = 4;
        controlPanel.add(new JLabel("New Status:"), gbc);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"PENDING", "ACCEPTED", "REJECTED"});
        gbc.gridx = 1;
        controlPanel.add(statusCombo, gbc);

        // Update Button
        JButton updateBtn = new JButton("Update Status");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        controlPanel.add(updateBtn, gbc);

        // Refresh and filter logic
        refreshBtn.addActionListener(e -> {
            List<Bid> bids = bidService.getBidsByLandlord(landlordId);

            // Filter
            String selectedStatus = (String) filterStatusCombo.getSelectedItem();
            if (!"ALL".equals(selectedStatus)) {
                bids.removeIf(bid -> !bid.getStatus().equalsIgnoreCase(selectedStatus));
            }

            // Sort
            String sortBy = (String) sortCombo.getSelectedItem();
            switch (sortBy) {
                case "Date ↑" -> bids.sort((a, b) -> a.getBidTimestamp().compareTo(b.getBidTimestamp()));
                case "Date ↓" -> bids.sort((a, b) -> b.getBidTimestamp().compareTo(a.getBidTimestamp()));
                case "Amount ↑" -> bids.sort((a, b) -> a.getAmount().compareTo(b.getAmount()));
                case "Amount ↓" -> bids.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
            }

            // Update UI
            bidArea.setText(formatBids(bids));
            bidCombo.removeAllItems();
            bids.forEach(bidCombo::addItem);
        });

        updateBtn.addActionListener(e -> {
            Bid selectedBid = (Bid) bidCombo.getSelectedItem();
            if (selectedBid == null) {
                JOptionPane.showMessageDialog(this, "Please select a bid first",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newStatus = (String) statusCombo.getSelectedItem();
            try {
                boolean success = bidService.updateBidStatus(
                        selectedBid.getBidId(), newStatus, landlordId);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Bid status updated successfully!");
                    refreshBtn.doClick();
                }
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.doClick(); // Load on first display

        return panel;
    }


    private static class BidListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Bid) {
                Bid bid = (Bid) value;
                String bidId = bid.getBidId().length() > 8 ? bid.getBidId().substring(0, 8) : bid.getBidId();
                String propId = bid.getPropertyId().length() > 8 ? bid.getPropertyId().substring(0, 8) : bid.getPropertyId();
                setText(String.format("BID %s - %s - $%,.2f - %s",
                        bidId, propId, bid.getAmount(), bid.getStatus()));
            }
            return this;
        }
    }
    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        JButton generateBtn = new JButton("Generate Report");

        generateBtn.addActionListener(e -> {
            String report = bidService.generateReports();
            outputArea.setText(report);

            try {
                PrinterJob job = PrinterJob.getPrinterJob();

                job.setPrintable((g, pageFormat, pageIndex) -> {
                    if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                    g.translate((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY());
                    outputArea.printAll(g);
                    return Printable.PAGE_EXISTS;
                });

                PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
                for (PrintService svc : services) {
                    if (svc.getName().toLowerCase().contains("pdf")) {
                        job.setPrintService(svc);
                        break;
                    }
                }

                HashPrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
                attrs.add(new Destination(new java.io.File("bid_report.pdf").toURI()));

                job.print(attrs);
                JOptionPane.showMessageDialog(null, "PDF created: bid_report.pdf");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        return panel;
    }


    private String formatBids(List<Bid> bids) {
        if (bids.isEmpty()) {
            return "No bids found for your properties\n\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s %-12s %-12s %-12s %-10s %-20s\n",
                "BID ID", "PROPERTY", "CLIENT", "AMOUNT", "STATUS", "DATE"));
        sb.append("----------------------------------------------------------------\n");

        for (Bid b : bids) {
            String bidId = b.getBidId().length() > 8 ? b.getBidId().substring(0, 8) : b.getBidId();
            String propId = b.getPropertyId().length() > 8 ? b.getPropertyId().substring(0, 8) : b.getPropertyId();
            String clientId = b.getClientId().length() > 8 ? b.getClientId().substring(0, 8) : b.getClientId();

            sb.append(String.format("%-12s %-12s %-12s $%-12.2f %-10s %-20s\n",
                    bidId,
                    propId,
                    clientId,
                    b.getAmount(),
                    b.getStatus(),
                    b.getBidTimestamp().toString().substring(0, 16)));
        }
        return sb.toString();
    }
}