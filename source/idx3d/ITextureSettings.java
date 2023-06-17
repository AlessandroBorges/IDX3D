package idx3d;

import java.util.Arrays;

import idx3d.ITexture;

public class ITextureSettings
{
	public ITexture texture;
	public int width;
	public int height;
	public int type;
	public float persistency;
	public float density;
	public int samples;
	public int numColors;
	public int[] colors;
	
	public ITextureSettings(ITexture tex, int w, int h, int t, float p, float d, int s, int[] c)
	{
		texture=tex;
		width=w;
		height=h;
		type=t;
		persistency=p;
		density=d;
		samples=s;
		colors=c;
	}

	@Override
	public String toString() {
		final int maxLen = 50;
		return "ITextureSettings [" + (texture != null ? "texture=" + texture + ", " : "") + "width=" + width
				+ ", height=" + height + ", type=" + type + ", persistency=" + persistency + ", density=" + density
				+ ", samples=" + samples + ", numColors=" + numColors + ", "
				+ (colors != null ? "colors=" + Arrays.toString(Arrays.copyOf(colors, Math.min(colors.length, maxLen)))
						: "")
				+ "]";
	}
	
	
	
	
	
}

		