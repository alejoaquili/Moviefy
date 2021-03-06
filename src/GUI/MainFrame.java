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

    /** The {@code MainFrame} class contains the logic operations of the controller and the view for the grafic
     *interface.
     *
     * @author Negro Caino, Ignacio Nicolás
     * @version 1.0
     */
public class MainFrame extends JFrame{

    private int width;
    private ArrayList<Movie>  movies;
    private MoviePanel mPanel;
    private LoadingPanel lPanel;
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
            movify = new MainFrame("Moviefy", api);
        }catch (IOException e){
            e.printStackTrace();
        }

        try{movify.start();} catch (IOException e){e.printStackTrace();}
    }

    /**Creates a {@code MainFrame} with a specified name and an API of a neural network.
     * @param name The frame name.
     * @param api The  API of the neural network.
     * @throws IOException if an I/O error occurs.
     */

    public MainFrame(String name,API api) throws IOException{
        super(name);
        this.api = api;
        Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        this.width = (int)(size.getWidth()*5)/8;
        setSize(this.width,(this.width*4)/5);
        setLocation(size.width/2 - getWidth()/2, size.height/2 - getHeight()/2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        setBackground(Color.WHITE);
        initialize();
    }

    /**This method put all the buttons and text in the panel.
     * @throws IOException if an I/O error occurs.
     */

    private void initialize() throws IOException{
        mPanel = new MoviePanel(0,0,width,this);
        mPanel.setVisible(false);
        add(mPanel);
        lPanel = new LoadingPanel(width);
        lPanel.setVisible(true);
        add(lPanel);
    }

    /**This method start the program.
     * @throws IOException if an I/O error occurs.
     */

    public void start() throws IOException{
        setVisible(true);
        userMakeQuery();
        showAndRateMovies();
    }

    /**This method filters the movies for the end user.
     */

    private void userMakeQuery(){
        query = new Query().setMaxYear(Calendar.getInstance().get(Calendar.YEAR)).setMinYear(2000);
    }
    /** This method begins the sequence of movies allowing the user rate each movie.
     * @throws IOException if an I/O error occurs.
     */
    private void showAndRateMovies() throws IOException{
        mPanel.nextMovie();
        mPanel.setVisible(true);
    }

    /** This method taggled the states of the program.
     * @param b The specified state loading or not.
     */

    public void loading(boolean b){
        if(!b)  mPanel.setFirstMovie();
        mPanel.setVisible(!b);
        lPanel.setVisible(b);
    }

    private class MoviePanel extends JPanel {
        private MainFrame mainFrame;
        private ArrayList<Poster> posters;
        private int width;
        private PosterLabel posterLabel;
        private JSlider ratingSlider;
        private int index;
        private LoadingLabel loadingLabel;
        private JLabel title;
        private JButton rateButton;
        private JButton neverSawItButton;
        private JTextArea synopsis;
        private JTextArea yearScore;
        private JTextArea genders;
        private JTextArea tags;
        private JTextArea actors;

        private MoviePanel(int x,int y,int width,MainFrame mainFrame){
            this.mainFrame = mainFrame;
            this.width = width;
            index = 0;
            setBackground(Color.white);
            setBounds(x,y,width,(int)(width*0.8));
            setLayout(null);
            initialize();
        }

        private void initialize(){
            posterLabel = new PosterLabel((((2*width)/3)-(width/48)),width/16,width/3);
            add(posterLabel);

            ratingSlider = new RatingSlider((((2*width)/3)-(width/48)),((7*width)/12),width/3,width/16);
            ratingSlider.setBackground(getBackground());
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
            title.setBackground(getBackground());
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
            posterLabel.setIcon(posters.get(index));
            try{synopsis.setText(m.getSynopsis());} catch (IOException e){synopsis.setText("No synopsis available");}
            yearScore.setText("Year: " + m.getYear() + "\tImdb Score : " + m.getIMDbScore());
            genders.setText("Genders: " + m.getGenre());
            tags.setText("Tags: " + m.getTags());
            actors.setText("Actors : ");
            for(Long l: m.getActorsID()){
                actors.append(m.getActor(l).toString() + ", ");
            }
        }

        void setFirstMovie(){
            setMovie(movies.get(0));
        }

        private void setEnableButtons(boolean b){
            neverSawItButton.setEnabled(b);
            rateButton.setEnabled(b);
        }

        private void nextMovie(){
            index++;
            if((movies == null)||(feedBack == null)) {
                movies = new ArrayList<>(api.getRecommendation(query));
                feedBack = new ArrayList<>();
                posters = new ArrayList<>();
                new PosterWorker(movies, posters, mainFrame).execute();
                index = 0;
            }else if(index >= movies.size()){
                api.sendFeedBack(feedBack);
                movies = new ArrayList<>(api.getRecommendation(query));
                feedBack = new ArrayList<>();
                posters = new ArrayList<>();
                new PosterWorker(movies,posters,mainFrame).execute();
                index = 0;
            }else{setMovie(movies.get(index));}
            setEnableButtons(true);

        }

        private class nextMoviesAction implements ActionListener{
            public void actionPerformed(ActionEvent e) {
                setEnableButtons(false);
                feedBack.add(new MovieFeedBack(movies.get(index),getRating()));
                nextMovie();
            }
        }

        private class seenItMovie implements ActionListener{
            public void actionPerformed(ActionEvent e){
                setEnableButtons(false);
                nextMovie();
            }
        }
    }
}
