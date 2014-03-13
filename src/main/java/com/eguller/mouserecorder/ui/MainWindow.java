package com.eguller.mouserecorder.ui;

import com.eguller.mouserecorder.config.Config;
import com.eguller.mouserecorder.format.def.DefaultFormat;
import com.eguller.mouserecorder.player.api.LoopEventListener;
import com.eguller.mouserecorder.player.api.Player;
import com.eguller.mouserecorder.player.event.LoopStartedEvent;
import com.eguller.mouserecorder.recorder.Record;
import com.eguller.mouserecorder.recorder.api.Recorder;
import com.eguller.mouserecorder.ui.action.*;
import com.eguller.mouserecorder.ui.state.ButtonStates;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * User: eguller
 * Date: 11/24/13
 * Time: 3:28 PM
 */
public class MainWindow extends JFrame implements Observer, LoopEventListener {
    JMenuBar menuBar;
    JButton playButton;
    JButton recordButton;
    JButton stopButton;


    JMenu fileMenu;
    JMenuItem saveItem;
    JMenuItem openItem;
    JSeparator separator;
    JMenuItem exitItem;

    JMenu optionMenu;
    JCheckBoxMenuItem minimizeOnRecordItem;
    JCheckBoxMenuItem minimizeOnPlayItem;
    InfiniteLoopMenuItem infiniteLoopMenuItem;
    LoopCountMenuItem loopCountMenuItem;
    SpeedMenuItem speedMenuItem;

    JMenu aboutMenu;
    JMenuItem aboutItem;
    JMenuItem helpItem;

    JLabel statusBar;

    Image startImage;
    Image recordImage;
    Image stopImage;

    JPanel buttonPanel;

    Recorder recorder;
    Player player;
    Config config;

    public MainWindow(Recorder recorder, Player player, Config config) {
        this.config = config;
        addComponentstoPane(getContentPane());
        this.setResizable(false);
        this.recorder = recorder;
        this.player = player;
        player.addObserver(this);
        player.addLoopEventListener(this);

    }

    private void addComponentstoPane(Container container) {
        loadImages();
        playButton = new JButton(new ImageIcon(startImage));
        playButton.setEnabled(false);
        playButton.addActionListener(new PlayAction());

        recordButton = new JButton(new ImageIcon(recordImage));
        recordButton.addActionListener(new RecordAction());

        stopButton = new JButton(new ImageIcon(stopImage));
        stopButton.addActionListener(new StopAction());
        stopButton.setEnabled(false);


        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        saveItem = new JMenuItem("Save");

        openItem = new JMenuItem("Open");
        separator = new JSeparator();
        exitItem = new JMenuItem("Exit");


        fileMenu.add(saveItem);
        fileMenu.add(openItem);
        fileMenu.add(separator);
        fileMenu.add(exitItem);

        aboutMenu = new JMenu("About");
        aboutItem = new JMenuItem("About");
        helpItem = new JMenuItem("Help");

        optionMenu = new JMenu("Options");
        minimizeOnRecordItem = new JCheckBoxMenuItem("Minimize on record");
        minimizeOnPlayItem = new JCheckBoxMenuItem("Minimize on play");

        optionMenu.add(minimizeOnRecordItem);
        optionMenu.add(minimizeOnPlayItem);
        optionMenu.addSeparator();

        infiniteLoopMenuItem = new InfiniteLoopMenuItem();
        optionMenu.add(infiniteLoopMenuItem);
        loopCountMenuItem = new LoopCountMenuItem(config);
        optionMenu.add(loopCountMenuItem);

        optionMenu.addSeparator();
        speedMenuItem = new SpeedMenuItem(config);
        optionMenu.add(speedMenuItem);


        menuBar.add(fileMenu);
        menuBar.add(optionMenu);
        menuBar.add(aboutMenu);


        statusBar = new JLabel();
        statusBar.setFont(statusBar.getFont().deriveFont(10.0f));
        statusBar.setHorizontalAlignment(SwingConstants.RIGHT);

        buttonPanel = new JPanel();
        buttonPanel.add(playButton, BorderLayout.WEST);
        buttonPanel.add(stopButton, BorderLayout.CENTER);
        buttonPanel.add(recordButton, BorderLayout.EAST);

        buttonPanel.setBorder(new EmptyBorder(3, 10, 3, 10));

        ButtonStates.PRERECORD.apply(this);

        container.add(menuBar, BorderLayout.PAGE_START);
        container.add(buttonPanel, BorderLayout.CENTER);
        container.add(statusBar, BorderLayout.PAGE_END);
        setResizable(false);

        setTitle("Mouse BaseRecorder");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);

        addActionListeners();
        loadConfig();
        centerScreen();
    }

    public void centerScreen() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
    }

    private static int config2SliderSpeed(double configSpeed) {
        return (int) Math.round(Math.log(configSpeed) / Math.log(2));
    }

    public void addActionListeners() {
        exitItem.addActionListener(new ExitAction());
        saveItem.addActionListener(new SaveFileAction(this, new DefaultFormat(config)));
        openItem.addActionListener(new OpenFileAction(this, new DefaultFormat(config)));
        minimizeOnRecordItem.addActionListener(new MinimizeOnRecordAction(config));
        minimizeOnPlayItem.addActionListener(new MinimizeOnPlayAction(config));
        infiniteLoopMenuItem.addActionListener(new InfiniteLoopAction(config, loopCountMenuItem));
    }

    public void loadConfig() {
        minimizeOnPlayItem.setState(config.getMinimizeOnPlay());
        minimizeOnRecordItem.setState(config.getMinimizeOnPlay());
        infiniteLoopMenuItem.setSelected(config.isInfiniteLoop());
        if (config.isInfiniteLoop()) {
            loopCountMenuItem.setVisible(false);
        } else {
            loopCountMenuItem.setVisible(true);
        }
        int speed = config2SliderSpeed(config.getSpeed());
        speedMenuItem.setSpeed(speed);
    }

    public void loadImages() {
        try {
            startImage = ImageIO.read(getClass().getResourceAsStream("/start.png"));
            recordImage = ImageIO.read(getClass().getResourceAsStream("/record.png"));
            stopImage = ImageIO.read(getClass().getResourceAsStream("/stop.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Record getRecord() {
        return recorder.getRecord();
    }

    public void setRecord(Record record) {
        recorder.setRecord(record);
    }

    @Override
    public void update(Observable observable, Object o) {
        ButtonStates.POSTPLAY.apply(MainWindow.this);

    }

    @Override
    public void loopStarted(LoopStartedEvent loopStartedEvent) {
        String message = "";
        if (loopStartedEvent.isInfiniteLoop()) {
            message = String.format("Loop %d of \\u221E", loopStartedEvent.getCurrentLoop() + 1);
        } else {
            message = String.format("Loop %d of %d", loopStartedEvent.getCurrentLoop() + 1, loopStartedEvent.getTotalLoop());
        }
        getStatusBar().setText(message);
    }

    /**
     * Record button action listener.
     */
    public class RecordAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ButtonStates.RECORDING.apply(MainWindow.this);
            minimizeOnRecord();
            recorder.record();
        }
    }

    public class PlayAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ButtonStates.PLAYING.apply(MainWindow.this);
            minimizeOnPlay();
            player.play(recorder.getRecord());

        }
    }

    public class StopAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ButtonStates.POSTRECORD.apply(MainWindow.this);
            recorder.stop();
        }
    }

    public void minimizeOnRecord() {
        if (config.getMinimizeOnRecord()) {
            this.setState(Frame.ICONIFIED);
        }
    }

    public void minimizeOnPlay() {
        if (config.getMinimizeOnPlay()) {
            this.setState(Frame.ICONIFIED);
        }
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JButton getPlayButton() {
        return playButton;
    }

    public JButton getRecordButton() {
        return recordButton;
    }

    public JLabel getStatusBar() {
        return statusBar;
    }

}
