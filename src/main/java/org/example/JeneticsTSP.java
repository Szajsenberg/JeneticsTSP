package org.example;

import io.jenetics.EnumGene;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class JeneticsTSP extends JFrame {

    final Integer cellSize = 20; // Wielkość każdej kratki
    final Integer numberOfBlocks = 50; // Liczba kratek
    static List<Integer> CitiesListX = new ArrayList<>(); // Lista przechowująca współrzędne X klikniętych komórek
    static List<Integer> CitiesListY = new ArrayList<>(); // Lista przechowująca współrzędne Y klikniętych komórek

    JPanel gridPanel;

    public JeneticsTSP() {
        // Tworzenie siatki o rozmiarze 50x50
        gridPanel = new JPanel(new GridLayout(numberOfBlocks, numberOfBlocks));

        // Reset środowiska
        reset();

        // Dodawanie siatki do głównego panelu
        getContentPane().add(gridPanel, BorderLayout.CENTER);

        // Nasłuchiwanie kliknięcia spacji
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && !CitiesListX.isEmpty()) {
                    startEngine();
                }
                else if (e.getKeyCode() == KeyEvent.VK_R) {
                    reset();
                }
            }
        });

        // Opcje okna
        setTitle("TMP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setFocusable(true);
        requestFocusInWindow();
    }

    // Metoda licząca odległość (nasza funkcja oceny)
    private static double eval(final int[] var) {
        double sum = 0;
        for (int i = 0; i < var.length; i++){
            int x1 = CitiesListX.get(var[i]);
            int y1 = CitiesListY.get(var[i]);
            int x2 = CitiesListX.get(var[(i+1) % var.length]);
            int y2 = CitiesListY.get(var[(i+1) % var.length]);
            sum += Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
        return sum;
    }

    // Metoda rysująca linię łączącą pierwszy i drugi element listy clickedCells
    private void drawLine(List<Integer> pointsOrder) {
        Graphics g = gridPanel.getGraphics();
        g.setColor(Color.RED);
        for(int i=1; i<pointsOrder.size();i++){
            g.drawLine(CitiesListX.get(pointsOrder.get(i-1))+cellSize/2,
                       CitiesListY.get(pointsOrder.get(i-1))+cellSize/2,
                       CitiesListX.get(pointsOrder.get(i))+cellSize/2,
                       CitiesListY.get(pointsOrder.get(i))+cellSize/2);
        }
    }

    // Metoda resetująca środowisko
    private void reset() {
        CitiesListX.clear();
        CitiesListY.clear();
        gridPanel.removeAll();
        for (int i = 0; i < numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                JPanel cell = new JPanel();
                cell.setBackground(Color.white);
                cell.setPreferredSize(new Dimension(cellSize, cellSize));
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        JPanel cell = (JPanel) e.getSource();
                        cell.setBackground(Color.black);
                        CitiesListX.add(cell.getX());
                        CitiesListY.add(cell.getY());
                    }
                });
                gridPanel.add(cell);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // Metoda obliczająca optymalne rozwiązanie używając algorytmu genetycznego
    void startEngine(){
        final Engine<EnumGene<Integer>, Double> engine = Engine
                .builder(
                        JeneticsTSP::eval,
                        Codecs.ofPermutation(CitiesListX.size()))
                .optimize(Optimize.MINIMUM)
                .maximalPhenotypeAge(10)
                .populationSize(1000)
                .build();

        final Phenotype<EnumGene<Integer>, Double> best =
                engine.stream()
                        .limit(5000)
                        .collect(EvolutionResult.toBestPhenotype());

        List<Integer> solutionOrder = best.genotype().chromosome().stream()
                .map(EnumGene::allele)
                .toList();

        // Wyświetlanie wyniku
        System.out.println(best);
        drawLine(solutionOrder);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JeneticsTSP window = new JeneticsTSP();
            window.setVisible(true);
        });
    }
}
