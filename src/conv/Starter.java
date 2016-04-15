package conv;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * 
 * @author PointerRage
 */
public class Starter {
	//private final static Logger log = LoggerFactory.getLogger(Starter.class);
	
	public static void main(String[] args) {
		File folder = new File("geodata");

		if(folder == null || !folder.exists() || !folder.isDirectory()) {
			System.out.println("folder 'geodata' not found!");
			System.exit(1);
		}

		System.out.println("Processing...");
		for(File f : folder.listFiles()) {
			final ConvType t = ConvType.L2j.isSupport(f) ? ConvType.L2j : ConvType.Dat.isSupport(f) ? ConvType.Dat : null;
			if(t == null) continue;
			
			int regx = Integer.parseInt(f.getName().substring(0, 2)),
				regy = Integer.parseInt(f.getName().substring(3, 5));
			
			try(RandomAccessFile raf = new RandomAccessFile(f, "r"); FileChannel fc = raf.getChannel()) {
				MappedByteBuffer buffer = fc.map(MapMode.READ_ONLY, 0, raf.length());
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				ByteBuffer convBuffer = t.convGeo(buffer);
				File out = new File(t.getFileName(regx, regy));
				out.createNewFile();
				try(RandomAccessFile rout = new RandomAccessFile(out, "rw"); FileChannel fcr = rout.getChannel()) {
					fcr.write(convBuffer);
				}
                CheckGeo.calculateHead(t.getFileName(regx, regy));
			} catch(Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
