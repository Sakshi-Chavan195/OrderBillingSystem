package billing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.FileOutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;

public class OrderBillingApp extends JFrame {

    private JTextField orderField;
    private JButton generateBtn;

    public OrderBillingApp() {
        setTitle("Order Billing System");
        setSize(400, 200);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("Enter Order Name:");
        orderField = new JTextField(20);
        generateBtn = new JButton("Generate Bill PDF");

        add(label);
        add(orderField);
        add(generateBtn);

        generateBtn.addActionListener(e -> generateBill());

        setVisible(true);
    }

    private void generateBill() {
        String orderName = orderField.getText().trim();
        if (orderName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter order name");
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/orderdb", "root", "root");
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM orders WHERE name = ?");
            stmt.setString(1, orderName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double total = qty * price;

                // Generate PDF
                Document doc = new Document();
                String fileName = "Order_Bill_" + name + ".pdf";
                PdfWriter.getInstance(doc, new FileOutputStream(fileName));
                doc.open();

                Font bold = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Paragraph heading = new Paragraph("Order Bill", bold);
                heading.setAlignment(Element.ALIGN_CENTER);
                doc.add(heading);
                doc.add(new Paragraph(" ")); // spacer

                doc.add(new Paragraph("Order Name: " + name));
                doc.add(new Paragraph("Quantity   : " + qty));
                doc.add(new Paragraph("Unit Price : ₹" + price));
                doc.add(new Paragraph("Total Cost : ₹" + total));

                doc.close();
                JOptionPane.showMessageDialog(this, "PDF generated: " + fileName);
            } else {
                JOptionPane.showMessageDialog(this, "Order not found in database!");
            }

            rs.close();
            stmt.close();
            con.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // Load JDBC driver (optional from Java 6 onwards)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("MySQL Driver not found.");
        }

        SwingUtilities.invokeLater(() -> new OrderBillingApp());
    }
}
