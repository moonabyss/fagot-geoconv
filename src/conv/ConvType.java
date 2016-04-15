package conv;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * @author PointerRage
 */
public enum ConvType {
	L2j { //to pts
		public boolean isSupport(File f) {
			return f.getName().endsWith(".l2j");
		}
		
		public String getFileName(int regx, int regy) {
			return regx + "_" + regy + "_conv.dat";
		}
		/*
		 * l2j:
		 * b - block type
		 * FLAT - 0 {
		 * 	s - height
		 * }
		 * COMPLEX 1 {
		 * 	x64 s - height (64 cells)
		 * }
		 * MULTILEVEL 2 {
		 * 	x64 {
		 * 		b - layers count
		 * 		x b {
		 * 			s - height
		 * 		}
		 * 	}
		 * }
		 * 
		 * pts:
		 * x18 b - header
		 * s - block type
		 * FLAT 0 {
		 * 	s - height
		 * 	s - height2
		 * }
		 * COMPLEX 4 {
		 * 	x64 s - height
		 * }
		 * MULTILEVEL 48 {
		 * 	s - layers count
		 * 	x s {
		 * 		s - height
		 * 	}
		 * }
		 */
		public ByteBuffer convGeo(ByteBuffer original) {
			ByteBuffer buffer = ByteBuffer.allocate(original.capacity() * 3);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.position(18);
			for(int i = 0; i < 65536; i++) {
				byte t = original.get();
				if(t == 0x00)
					buffer.putShort((short) 0x00);
				else if(t == 0x01)
					buffer.putShort((short) 0x0040);
				else buffer.putShort((short) 0x0048);
				
				if(t == 0x00) {
					short val = original.getShort();
					buffer.putShort((short) (val + 45)); //max height
					buffer.putShort(val);
				} else if(t == 0x01) {
					for(int j = 0; j < 64; j++)
						buffer.putShort(original.getShort());
				} else {
					for(int j = 0; j < 64; j++) {
						byte layers = original.get();
						buffer.putShort(layers);
						for(int l = 0; l < layers; l++)
							buffer.putShort(original.getShort());
					}
				}
			}
			buffer.flip();
			buffer.position(0);
			return buffer;
		}
	},
	Dat { //to java
		public boolean isSupport(File f) {
			return f.getName().endsWith("_conv.dat");
		}
		
		public String getFileName(int regx, int regy) {
			return regx + "_" + regy + ".l2j";
		}
		
		public ByteBuffer convGeo(ByteBuffer original) {
			ByteBuffer buffer = ByteBuffer.allocate(original.capacity());
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			original.position(18);
			for(int i = 0; i < 65536; i++) {
				short t = original.getShort();
				if(t == 0x00)
					buffer.put((byte) 0x00);
				else if(t == 0x0040)
					buffer.put((byte) 0x01);
				else buffer.put((byte) 0x02);
				if(t == 0x00) {
					original.getShort();
					buffer.putShort(original.getShort());
				} else if(t == 0x0040) {
					for(int j = 0; j < 64; j++)
						buffer.putShort(original.getShort());
				} else {
					for(int j = 0; j < 64; j++) {
						short layers = original.getShort();
						buffer.put((byte)layers);
						for(int l = 0; l < layers; l++)
							buffer.putShort(original.getShort());
					}
				}
			}
			buffer.flip();
			buffer.position(0);
			return buffer;
		}
	}, NONE{

		@Override
		public boolean isSupport(File f) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String getFileName(int regx, int regy) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ByteBuffer convGeo(ByteBuffer original) {
			// TODO Auto-generated method stub
			return null;
		}};
	
	public abstract boolean isSupport(File f);
	public abstract String getFileName(int regx, int regy);
	public abstract ByteBuffer convGeo(ByteBuffer original);
}
