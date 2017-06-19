package GUI;

import Model.*;
import Model.NeuralNetwork.API;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainFrame extends JFrame{
    //main

    //MainFrame
    private int width;
    private ArrayList<Movie>  movies;
    private Movie   actualMovie;
    private MoviePanel mPanel;
    private JLabel  title;
    private ArrayList<MovieFeedBack> feedBack;
    private Query query;
    private API api;

    public static void main(String[] args) {
        Application app = null;
        MainFrame movify = null;

        try {
            if(System.getProperty("os.name").contains("Windows")){
                app = new Application(".\\db\\movieDB.csv", ".\\db\\personDB.csv");
            }else {
                app = new Application("./db/movieDB.csv", "./db/personDB.csv");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        API api = new API(app, 5, 7);

        try {
            movify = new MainFrame("Movify", 920, api);
        }catch (IOException e){
            e.printStackTrace();
        }

        try{movify.start();} catch (IOException e){e.printStackTrace();}
    }


    public MainFrame(String name, int width,API api) throws IOException{
        super(name);
        this.width = width;
        this.api = api;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setSize(width,(width*4)/5);
        setResizable(false);
        Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation(size.width/2 - getWidth()/2, size.height/2 - getHeight()/2);
        initialize();
    }

    private void initialize() throws IOException{
        mPanel = new MoviePanel(0,0,width);
        mPanel.setVisible(false);
        add(mPanel);
    }

    public void start() throws IOException{
        setVisible(true);
        //        welcome();
        userMakeQuery();
        showAndRateMovies();
    }

    /* will be changed when the query maker panel exist (?*/
    private void userMakeQuery(){
        query = new Query().setMaxYear(Calendar.getInstance().get(Calendar.YEAR)).setMinYear(2000);
    }

    private void showAndRateMovies() throws IOException{
        //set all visibilities off but no mPanel
        movies = new ArrayList<>(api.getRecommendation(query));
        feedBack = new ArrayList<>();
        mPanel.setMovie(movies.get(0));
        mPanel.setVisible(true);
    }

    private class MoviePanel extends JPanel {
        private int width;
        private PosterLabel posterLabel;
        private JSlider ratingSlider;
        private int index;
        private JLabel title;
        private JButton rateButton;
        private JButton neverSawItButton;
        private JTextArea synopsis;
        private JTextArea yearScore;
        private JTextArea genders;
        private JTextArea tags;
        private JTextArea actors;

        private MoviePanel(int x,int y,int width){
            this.width = width;
            index = 0;
            setBounds(x,y,width,(width*17)/23);
            setLayout(null);
            initialize();
        }

        private void initialize(){
            posterLabel = new PosterLabel((((2*width)/3)-(width/48)),width/16,width/3);
            add(posterLabel);

            ratingSlider = new RatingSlider((((2*width)/3)-(width/48)),((7*width)/12),width/3,width/16);
            add(ratingSlider);

            rateButton = new JButton("Rate");
            rateButton.setBounds((((2*width)/3)-(width/48)),((width*125)/184),width/3,width/24);
            rateButton.addActionListener(new nextMoviesAction());
            rateButton.setFont(new Font(rateButton.getFont().getName(),Font.PLAIN,(int)(rateButton.getHeight()*0.9)));
            add(rateButton);

            neverSawItButton = new JButton("Never saw it");
            neverSawItButton.setBounds((width*7)/24,((width*125)/184),width/3,width/24);
            neverSawItButton.addActionListener(new seenItMovie());
            neverSawItButton.setFont(new Font(neverSawItButton.getFont().getName(),Font.PLAIN,(int)(neverSawItButton.getHeight()*0.9)));
            add(neverSawItButton);

            title = new JLabel("");
            title.setBounds(width/48,width/48,width-(width/48),width/24);
            title.setFont(new Font(title.getFont().getName(),Font.BOLD,(int)(title.getHeight()*0.9)));
            add(title);

            synopsis = new SynopsisTextArea(width/48,width/16,(width*3)/5,width/10);
            synopsis.setBackground(getBackground());
            add(synopsis);

            yearScore = new JTextArea();
            yearScore.setBackground(getBackground());
            yearScore.setBounds(width/48,synopsis.getHeight()+synopsis.getY(),synopsis.getWidth(),synopsis.getHeight()/4);
            yearScore.setFont(synopsis.getFont());
            add(yearScore);

            genders = new JTextArea();
            genders.setBackground(getBackground());
            genders.setBounds(width/48,yearScore.getHeight()+yearScore.getY(),yearScore.getWidth(),yearScore.getHeight());
            genders.setFont(synopsis.getFont());
            add(genders);

            tags = new JTextArea();
            tags.setBackground(getBackground());
            tags.setBounds(width/48,genders.getHeight()+genders.getY(),yearScore.getWidth(),yearScore.getHeight()*2);
            tags.setFont(synopsis.getFont());
            tags.setWrapStyleWord(true);
            tags.setLineWrap(true);
            add(tags);

            actors = new JTextArea();
            actors.setBackground(getBackground());
            actors.setBounds(width/48,tags.getHeight()+tags.getY(),synopsis.getWidth(),synopsis.getHeight());
            actors.setWrapStyleWord(true);
            actors.setLineWrap(true);
            actors.setFont(synopsis.getFont());
            add(actors);

        }

        public int getRating(){
            return ratingSlider.getValue();
        }

        public void setMovie(Movie m){
            if(m == null) throw new IllegalArgumentException();
            title.setText(m.getTitle());
            try{
                posterLabel.setIcon(new Poster(m.getPosterURL()));
            }catch (IOException e){
                posterLabel.setIcon(new Poster(new ImageIcon("db/2000px-No_image_available.svg.png")));
            }
            try{synopsis.setText(m.getSynopsis());} catch (IOException e){synopsis.setText("No synopsis available");}
            yearScore.setText("Year: " + m.getYear() + "\tImdb Score : " + m.getIMDbScore());
            genders.setText("Genders: " + m.getGenre());
            tags.setText("Tags: " + m.getTags());
            actors.setText("Actors : ");
            for(Long l: m.getActorsID()){
                actors.append(m.getActor(l).toString() + ", ");
            }
        }

        private void nextMovie(){
            index++;
            if(index >= movies.size()){
                api.sendFeedBack(feedBack);
                movies = new ArrayList<>(api.getRecommendation(query));
                feedBack = new ArrayList<>();
                index = 0;
            }
            setMovie(movies.get(index));
        }

        private class nextMoviesAction implements ActionListener{
            public void actionPerformed(ActionEvent e) {
                System.out.println("Rating : " + getRating());
                feedBack.add(new MovieFeedBack(movies.get(index),getRating()));
                nextMovie();
            }
        }

        private class seenItMovie implements ActionListener{
            public void actionPerformed(ActionEvent e){
                nextMovie();
            }
        }
    }



}