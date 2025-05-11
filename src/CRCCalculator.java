import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CRCCalculator extends JFrame {
    private JTextField bitStreamField;
    private JTextField repetitionsField;
    private JTextArea resultArea;
    private JButton calculateButton;

    public CRCCalculator() {
        setTitle("Kalkulator CRC dla sieci CAN");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 5, 5));

        inputPanel.add(new JLabel("Ciąg bitowy (max 96 bitów):"));
        bitStreamField = new JTextField();
        inputPanel.add(bitStreamField);

        inputPanel.add(new JLabel("Liczba powtórzeń (1-10^9):"));
        repetitionsField = new JTextField("1");
        inputPanel.add(repetitionsField);

        calculateButton = new JButton("Oblicz");
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateCRC();
            }
        });
        inputPanel.add(new JLabel(""));
        inputPanel.add(calculateButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void calculateCRC() {
        String bitStreamText = bitStreamField.getText().trim();

        if (!bitStreamText.matches("[01]+")) {
            resultArea.setText("Błąd: Ciąg bitowy może zawierać tylko znaki 0 i 1.");
            return;
        }

        if (bitStreamText.length() > 96) {
            resultArea.setText("Błąd: Ciąg bitowy nie może przekraczać 96 bitów.");
            return;
        }

        long repetitions;
        try {
            repetitions = Long.parseLong(repetitionsField.getText().trim());
            if (repetitions < 1 || repetitions > 1_000_000_000) {
                resultArea.setText("Błąd: Liczba powtórzeń musi być w zakresie od 1 do 10^9.");
                return;
            }
        } catch (NumberFormatException e) {
            resultArea.setText("Błąd: Nieprawidłowy format liczby powtórzeń.");
            return;
        }

        int[] bitStream = new int[bitStreamText.length()];
        for (int i = 0; i < bitStreamText.length(); i++) {
            bitStream[i] = bitStreamText.charAt(i) == '1' ? 1 : 0;
        }

        resultArea.setText("Obliczanie CRC...\n");

        // Pomiar czasu
        long startTime = System.nanoTime();

        int crc = 0;
        for (long i = 0; i < repetitions; i++) {
            crc = calculateCRCValue(bitStream);
        }

        long endTime = System.nanoTime();
        long totalTimeNs = endTime - startTime;
        double totalTimeMs = totalTimeNs / 1_000_000.0;
        double averageTimeNs = (double) totalTimeNs / repetitions;

        // Wyświetlenie wyniku
        StringBuilder result = new StringBuilder();
        result.append("Suma kontrolna CRC: 0x").append(String.format("%04X", crc)).append("\n");
        result.append("Całkowity czas: ").append(String.format("%.2f", totalTimeMs)).append(" ms\n");
        result.append("Średni czas obliczenia CRC: ").append(String.format("%.2f", averageTimeNs)).append(" ns\n");

        resultArea.setText(result.toString());
    }

    private int calculateCRCValue(int[] bitStream) {
        int crc = 0;

        for (int nxtbit : bitStream) {
            int crcnxt = nxtbit ^ ((crc >> 14) & 1);
            crc = (crc << 1) & 0x7FFE;
            if (crcnxt == 1) {
                crc ^= 0x4599;
            }
        }
        return crc;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CRCCalculator calculator = new CRCCalculator();
            calculator.setVisible(true);
        });
    }
}
