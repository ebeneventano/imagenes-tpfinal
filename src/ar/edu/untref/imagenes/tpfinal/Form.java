package ar.edu.untref.imagenes.tpfinal;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import boofcv.io.image.SimpleImageSequence;
import boofcv.io.video.VideoMjpegCodec;
import boofcv.io.wrapper.images.JpegByteImageSequence;
import boofcv.struct.image.ImageFloat32;

@SuppressWarnings("serial")
public class Form extends JFrame{
	
	private JScrollPane scrollPane;
	private JPanel contentPane;

	private JMenuBar menuBar;
	private JMenu menuArchivo;
	private JMenuItem menuOpenImage;
	private JLabel labelPrincipalImage;
	private JMenu menuSift;
	
	private JMenuItem menuAplicarSift;
	
	private JMenu menuPcaSift;
	
	private JMenuItem menuAplicarPcaSift;
	
	private File querySift;
	private File targetSift;
	
	public Form(){

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		
		contentPane = new JPanel();
		setContentPane(contentPane);

		scrollPane = new JScrollPane();
		scrollPane.setViewportView(labelPrincipalImage);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		initComponents();
	}
	
	private void initComponents() {
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		menuArchivo = new JMenu("Archivo");
		menuBar.add(menuArchivo);
		
		menuSift = new JMenu("Sift");
		menuBar.add(menuSift);
		
		menuAplicarSift = new JMenuItem("Aplicar Sift");
		menuSift.add(menuAplicarSift);
		
		menuPcaSift = new JMenu("PCA - Sift");
		menuAplicarPcaSift = new JMenuItem("Aplicar PCA - Sift");
		menuPcaSift.add(menuAplicarPcaSift);
		menuBar.add(menuPcaSift);

		menuOpenImage = new JMenuItem("Abrir Imagen");
		menuArchivo.add(menuOpenImage);
		
		menuOpenImage.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				abrirImagen();
			}
		});
		
		menuAplicarSift.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aplicarMetodoSift();
			}

		});
		
		menuAplicarPcaSift.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aplicarMetodoPcaSift();
			}

		});
	}

	public void abrirImagen() {

		JFileChooser selector = new JFileChooser();
		selector.setDialogTitle("Seleccione una imagen");

		int flag = selector.showOpenDialog(null);

		if (flag == JFileChooser.APPROVE_OPTION) {
			try {

				File archivoSeleccionado = selector.getSelectedFile();
				BufferedImage imageSelected = ImageIO.read(archivoSeleccionado);
				if(imageSelected.getHeight() != 376 || imageSelected.getWidth() != 499){
					BufferedImage scaledImg = new BufferedImage(499, 376, BufferedImage.TYPE_INT_RGB);
					scaledImg.createGraphics().drawImage(imageSelected, 0, 0, 499, 376, null);
					guardarImagen(archivoSeleccionado.getAbsolutePath(), scaledImg);
					addImageToForm(new File(archivoSeleccionado.getAbsolutePath()));
				}else{
					addImageToForm(archivoSeleccionado);
				}
				
				
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		
		this.setVisible(true);
	}
	
    
    public void guardarImagen(String filePath, BufferedImage image){
    	File outputfile = new File(filePath);
    	try {
			ImageIO.write(image, "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addImageToForm(File archivoSeleccionado) throws FileNotFoundException{
		Class imageType = ImageFloat32.class;
		 
		VideoMjpegCodec codec = new VideoMjpegCodec();
		List<byte[]> data = codec.read(new FileInputStream(archivoSeleccionado));
		SimpleImageSequence sequence = new JpegByteImageSequence(imageType,data,true);
 
		ExamplePointFeatureTracker app = new ExamplePointFeatureTracker(imageType);
 
		app.createSURF();
 
		app.process(sequence);
	}
	
	private void aplicarMetodoSift() {
		this.seleccionarQuerySift();
		this.seleccionarTargetSift();
		
		try {
			Sift.aplicar(querySift, targetSift);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void seleccionarQuerySift() {
		JFileChooser selector = new JFileChooser();
		selector.setDialogTitle("Seleccione la primer imagen a comparar");

		int flag = selector.showOpenDialog(null);

		if (flag == JFileChooser.APPROVE_OPTION) {
			this.querySift = selector.getSelectedFile();
		}
	}
	
	public void seleccionarTargetSift() {
		JFileChooser selector = new JFileChooser();
		selector.setDialogTitle("Seleccione la segunda imagen a comparar");

		int flag = selector.showOpenDialog(null);

		if (flag == JFileChooser.APPROVE_OPTION) {
			this.targetSift = selector.getSelectedFile();
		}
	}

	private void aplicarMetodoPcaSift() {
		this.seleccionarQuerySift();
		this.seleccionarTargetSift();
		
		try {
			PcaSift.aplicar(querySift, targetSift);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
