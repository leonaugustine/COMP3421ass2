package ass2.spec;

import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;

import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.swing.JLabel;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Camera implements GLEventListener {
	final public boolean debug = Game.debug;
	double xtrans = 0.0;
	double ytrans = 0.0;
	double ztrans = 0.0;
	int yrotate = 0;
	Terrain myTerrain;
	GLU glu = new GLU();
	float ambient = 0.7f;
	float diffuse = 1.5f;
	float specular = 0.3f;
	JLabel myLabel = Game.label;
	boolean lightEnable = true;
	double cameraAvatarDistance = 5.0;
	boolean flipped = false;
	public double avatarAngle = 0.0;
	public double angleStep = 10.0;
	private double worldRotate = 0.0;

	public Camera(Terrain theTerrain) {
		myTerrain = theTerrain;
	}

	@Override
	public void display(GLAutoDrawable arg0) {
		GL2 gl = arg0.getGL().getGL2();
		if (debug) {
			System.out.println("display method working");
		}
		// clear both the colour buffer and the depth buffer
		// 设置背景为天蓝色
		gl.glClearColor(0.0f, 245 / 255f, 1.0f, 0.5f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		if (Game.worldCamera == false) {
			double avatarRadius = avatarAngle * Math.PI / 180.0;
			double eyex = myTerrain.getAvatar().x - Math.sin(avatarRadius)
					* cameraAvatarDistance;
			double eyey = myTerrain.getAvatar().getY() + 3.0;
			double eyez = myTerrain.getAvatar().z - Math.cos(avatarRadius)
					* cameraAvatarDistance;

			if (!flipped) {
				glu.gluLookAt(eyex, eyey, eyez, myTerrain.getAvatar().x,
						myTerrain.getAvatar().getY(), myTerrain.getAvatar().z,
						0, 1, 0);
			} else {
				glu.gluLookAt(eyex, -eyey, eyez, myTerrain.getAvatar().x,
						myTerrain.getAvatar().getY(), myTerrain.getAvatar().z,
						0, -1, 0);
			}

		} else { // For World Camera
			double[] worldMidPoint = {
					myTerrain.size().getWidth() / 2,
					myTerrain.altitude(myTerrain.size().getWidth()-1, myTerrain
							.size().getHeight()-1) / 2,
					myTerrain.size().getHeight() / 2 };
			double eyex = worldMidPoint[0]
					+ Math.sin(worldRotate * Math.PI / 180)*(cameraAvatarDistance+ztrans);
			double eyey = worldMidPoint[0] ;
			double eyez = worldMidPoint[2]
					- Math.cos(worldRotate * Math.PI / 180)*(cameraAvatarDistance+ztrans);
			if (!flipped) {
				glu.gluLookAt(eyex, eyey, eyez, worldMidPoint[0],
						worldMidPoint[1], worldMidPoint[2], 0, 1, 0);
			} else {
				glu.gluLookAt(eyex, -eyey, eyez, worldMidPoint[0],
						worldMidPoint[1], worldMidPoint[2], 0, -1, 0);
			}

		}
		gl.glPushMatrix();

		if (lightEnable) {
			setLighting(gl);
		}

		// 现在GL在地图应在位置
		drawShape(gl);
		myLabel.setText("<HTML>Avatar Position<P>X:" + myTerrain.getAvatar().x
				+ "<P>Y:" + myTerrain.getAvatar().y + "<P>Z:"
				+ myTerrain.getAvatar().z + "<P>angle:" + avatarAngle
				+ "<P>Light:" + "<P>Sunlight direction:"
				+ myTerrain.getSunlight()[0] + " " + myTerrain.getSunlight()[1]
				+ " " + myTerrain.getSunlight()[2] + " " + "<P>luminance"
				+ "<P>ambient " + ambient + "<P>diffuse" + diffuse + "<P>Time"
				+ time + "<P>sun Color: " + color[0] * 255 + " " + color[1]
				* 255 + " " + color[2] * 255 + "</HTML>");
	}

	// public double sunRotate = 0.0;
	public double time = 0; // 0~24, 25frame per hour in world
	// time=14时，sunrotate=0
	public double[] lightDirAt8 = { -1, 0, 0, 1 };
	double[] color = { 1.0, 1.0, 1.0 };

	@SuppressWarnings("unused")
	private double[] getLightDirection() {
		double angle = (time - 8) / 24 * 360.0;
		double[] lightDir = MathUtil
				.multiply(MathUtil.rotationMatrix(angle, false, false, true),
						lightDirAt8);
		return lightDir;
	}

	private void setLighting(GL2 gl) {
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glPushMatrix();
		if (Game.originSun == false) {
			time += 0.04;
			if (time >= 24) {
				time = 0;
			}
			double sunRotate = time * 15;
			gl.glRotated(sunRotate, 0.0, 0.0, 1.0); // 用于太阳的旋转,working now
			if (debug) {
				System.out.println("LIghting lumi:ambient " + ambient
						+ " diffuse " + diffuse);
			}
			if (time >= 0 && time < 8) { // 蓝
				color[0] = 0.0;
				color[1] = 102 / 255d;
				color[2] = 1.0;
				// blue = 0,102,255
			} else if (time >= 8 && time <= 18) { // 蓝->黄
				// yellow = 255,255,51
				color[0] = (time - 8) / (18 - 8); // 0->255
				color[1] = (102 + (255 - 102) * (time - 8) / (18 - 8)) / 255; // 102->255
				color[2] = (255 - (255 - 51) * (time - 8) / (18 - 8)) / 255; // 255->51
			} else if (time > 18 && time <= 19) {
				color[0] = 1.0;
				color[1] = 1.0;
				color[2] = 51 / 255d;
			} else if (time > 19 && time < 24) {// 黄->蓝
				color[0] = 1 - (time - 19) / (24 - 19); // 255->0
				color[1] = (255 - (255 - 102) * (time - 19) / (24 - 19)) / 255;
				color[2] = (51 + (255 - 51) * (time - 19) / (24 - 19)) / 255;
			}
			float[] a = new float[4];
			a[0] = (float) (color[0] * ambient);
			a[1] = (float) (color[1] * ambient);
			a[2] = (float) (color[2] * ambient);
			// a[0] = a[1] = a[2] = ambient;
			a[3] = 1.0f;
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, a, 0); // a是亮度,位置和LIGHT0绑在一起了
			float[] ambientPos = new float[] { 1.0f, 0.0f, 1.0f, 1.0f }; // 最后一个数值,1是位置,0是方向
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, ambientPos, 0);

			float[] d = new float[4];
			d[0] = (float) (color[0] * diffuse);
			d[1] = (float) (color[1] * diffuse);
			d[2] = (float) (color[2] * diffuse);
			// d[0] = d[1] = d[2] = diffuse;
			d[3] = 1.0f;
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, d, 0);
			float[] diffusePos = new float[] { 1.0f, 5.0f, 5.0f, 0.0f }; // 方向性
			diffusePos[0] = myTerrain.getSunlight()[0];
			diffusePos[1] = myTerrain.getSunlight()[1];
			diffusePos[2] = myTerrain.getSunlight()[2];

			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, diffusePos, 0);

		} else {
			float[] a = new float[4];
			a[0] = a[1] = a[2] = ambient;
			a[3] = 1.0f;
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, a, 0); // a是亮度,位置和LIGHT0绑在一起了
			float[] ambientPos = new float[] { 1.0f, 0.0f, 1.0f, 1.0f }; // 最后一个数值,1是位置,0是方向
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, ambientPos, 0);

			float[] d = new float[4];
			d[0] = d[1] = d[2] = diffuse;
			d[3] = 1.0f;
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, d, 0);
			float[] diffusePos = new float[] { 1.0f, 5.0f, 5.0f, 0.0f }; // 方向性
			diffusePos[0] = myTerrain.getSunlight()[0];
			diffusePos[1] = myTerrain.getSunlight()[1];
			diffusePos[2] = myTerrain.getSunlight()[2];
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, diffusePos, 0);

		}
		gl.glPopMatrix();
	}

	// 真正的画图的方法。由于terrian内部含有tree之类的方法，所以只要载入terrian就好了
	private void drawShape(GL2 gl) {
		// myTerrain.triangleTest(gl);
		drawCoor(gl);
		myTerrain.draw(gl, groundTexture, treeTexture, roadTexture);

	}

	private void drawCoor(GL2 gl) {
		gl.glColor3f(0.0f, 0.0f, 1.0f);// blue
		gl.glColor3d(1.0, 0.0, 0.0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0.0, 0.0, 0.0);
		gl.glVertex3d(30.0, 0.0, 0.0);
		gl.glColor3d(0.0, 1.0, 0.0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 30, 0);
		gl.glColor3d(0.0, 0.0, 1.0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 0, 30);
		gl.glEnd();
	}

	Texture groundTexture;
	Texture treeTexture;
	Texture leafTexture;
	Texture roadTexture;

	private void setTexture(GL2 gl) {
		// Load texture from image
		try {
			// Create a OpenGL Texture object from (URL, mipmap, file suffix)
			// Use URL so that can read from JAR and disk file.
			groundTexture = TextureIO.newTexture(getClass().getClassLoader()
					.getResource("ground.jpg"), // relative to project root
					false, ".jpg");
			treeTexture = TextureIO.newTexture(getClass().getClassLoader()
					.getResource("tree.jpg"), // relative to project root
					false, ".jpg");
			roadTexture = TextureIO.newTexture(getClass().getClassLoader()
					.getResource("road.jpg"), // relative to project root
					false, ".jpg");

			// Use linear filter for texture if image is larger than the
			// original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			// Use linear filter for texture if image is smaller than the
			// original texture
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(GLAutoDrawable arg0) { // 获取GL对象
		GL2 gl = arg0.getGL().getGL2();
		gl.glLoadIdentity();
		if (lightEnable) {

		}
		// 启用纹理映射
		gl.glEnable(GL2.GL_TEXTURE_2D);
		// 设置混合
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL.GL_ONE);
		// 启用颜色混合
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		 gl.glColorMaterial( gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT_AND_DIFFUSE);
		// );
		// 启用阴影平滑 /启用高斯模糊
		gl.glShadeModel(GL2.GL_SMOOTH);
		// 设置背景颜色为黑色
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		// 设置深度缓存
		gl.glClearDepth(1.0f);
		// 启用深度测试
		gl.glEnable(GL.GL_DEPTH_TEST);
		// 所作的深度测试的类型
		gl.glDepthFunc(GL.GL_LEQUAL);
		// 告诉系统对透视进行修正
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

		// Really Nice Perspective Calculations
		setTexture(gl);

	}

	/**
	 * 窗口变换大小
	 */
	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width,
			int height) {
		final GLU glu = new GLU();
		if (debug) {
			System.out.println("reshape method working");
		}

		final GL2 gl = glDrawable.getGL().getGL2();

		// enable depth testing
		gl.glEnable(GL.GL_DEPTH_TEST);
		if (lightEnable) {
			// enable lighting, turn on light 0
			gl.glEnable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_LIGHT0);
			gl.glEnable(GL2.GL_LIGHT1);
			// normalise normals (!)
			// this is necessary to make lighting work properly
			gl.glEnable(GL2.GL_NORMALIZE);
		}
		// 防止被零除
		if (height <= 0) // avoid a divide by zero error!
			height = 1;
		final float h = (float) width / (float) height;
		// 设置视窗的大小
		gl.glViewport(0, 0, width, height);
		// 选择投影矩阵 ,投影矩阵负责为我们的场景增加透视。
		gl.glMatrixMode(GL2.GL_PROJECTION);
		// 重置投影矩阵;
		gl.glLoadIdentity();
		// 设置视口的大小
		// glu.gluOrtho2D(1.0, 1.0, 1.0, 1.0);这个是平行的，么有透视
		glu.gluPerspective(45.0f, h, 1.0, 50.0); // --眼睛睁开的角度 ，比例，近，远
		// 启用模型观察矩阵；模型观察矩阵中存放了我们的物体讯息。
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void rotateRight() {
		if (debug) {
			System.out.println("keyboard: Rotata Right");
		}
		worldRotate += 10;

	}

	public void rotateLeft() {
		if (debug) {
			System.out.println("keyboard: Rotate LEft");
		}
		worldRotate += -10;
	}

	public void flip() {

		if (flipped == true) {
			flipped = false;
		} else {
			flipped = true;
		}
	}

	/**
	 * The camera should move up and down following the terrain. So if you move
	 * it forward up a hill, the camera should move up the hill.
	 * 镜头要随着山坡移动(永远在山坡上方一定距离,如果距离山坡小于某个距离,则自动上抬)
	 */
	public void stepForward() {
		if (debug) {
			System.out.println("keyboard: Step Forward");
		}
		ztrans += 0.5;

	}

	public void stepBackward() {
		if (debug) {
			System.out.println("keyboard: Step Backward");
		}
		ztrans += -0.5;
	}

	public void ambientUp() {
		ambient += 0.1f;
	}

	public void ambientDown() {
		ambient += -0.1f;
	}

	public void diffuseUp() {
		diffuse += 0.1f;
	}

	public void diffuseDown() {
		diffuse += -0.1f;
	}

	public void specularUp() {
		specular += 0.1f;
	}

	public void specularDown() {
		specular += -0.1f;
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {

	}

	public void avatarStepForward() {
		myTerrain.getAvatar().stepForawrd(avatarAngle);
	}

	public void avatarStepBorward() {
		myTerrain.getAvatar().stepBackward(avatarAngle);
	}

	public void avatarRotateCCW() {
		avatarAngle += angleStep;
	}

	public void avatarRotateCW() {
		avatarAngle -= angleStep;
	}

}
