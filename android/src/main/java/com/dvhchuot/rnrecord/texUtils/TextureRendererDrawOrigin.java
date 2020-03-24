package com.dvhchuot.rnrecord.texUtils;

import android.opengl.GLES20;

/**
 * Created by wangyang on 15/7/23.
 */
public class TextureRendererDrawOrigin extends TextureRenderer {

    private static final String fshDrawOrigin = "" +
            "precision mediump float;\n" +
            "varying vec2 texCoord;\n" +
            "uniform %s inputImageTexture;\n" +
            "void main()\n" +
            "{\n" +
            "   gl_FragColor = texture2D(inputImageTexture, texCoord);\n" +
            "}";

//    private static final String fshDrawOrigin = "" +
//            "precision lowp float;" +
//
//            "varying highp vec2 vTextureCoord;" +
//            "uniform lowp sampler2D sTexture;" +
//            "uniform float intensity;" +
//
//            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);" +
//
//            "void main() {" +
//
//            "lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);" +
//            "float luminance = dot(textureColor.rgb, luminanceWeighting);" +
//
//            "lowp vec4 desat = vec4(vec3(luminance), 1.0);" +
//
//            "lowp vec4 outputColor = vec4(" +
//            "(desat.r < 0.5 ? (2.0 * desat.r * 0.6) : (1.0 - 2.0 * (1.0 - desat.r) * (1.0 - 0.6)))," +
//            "(desat.g < 0.5 ? (2.0 * desat.g * 0.45) : (1.0 - 2.0 * (1.0 - desat.g) * (1.0 - 0.45)))," +
//            "(desat.b < 0.5 ? (2.0 * desat.b * 0.3) : (1.0 - 2.0 * (1.0 - desat.b) * (1.0 - 0.3)))," +
//            "1.0" +
//            ");" +
//
//            "gl_FragColor = vec4(mix(textureColor.rgb, outputColor.rgb, intensity), textureColor.a);" +
//            "}";


    public TextureRendererDrawOrigin() {
        defaultInitialize();
    }

    TextureRendererDrawOrigin(boolean noDefaultInitialize) {
        if(!noDefaultInitialize)
            defaultInitialize();
    }

    public static TextureRendererDrawOrigin create(boolean isExternalOES) {
        TextureRendererDrawOrigin renderer = new TextureRendererDrawOrigin();
        if(!renderer.init(isExternalOES)) {
            renderer.release();
            return null;
        }
        return renderer;
    }

    @Override
    public boolean init(boolean isExternalOES) {
        return setProgramDefualt(getVertexShaderString(), getFragmentShaderString(), isExternalOES);
    }

    @Override
    public void release() {
        GLES20.glDeleteBuffers(1, new int[]{mVertexBuffer}, 0);
        mVertexBuffer = 0;
        mProgram.release();
        mProgram = null;
    }

    @Override
    public void renderTexture(int texID, Viewport viewport) {

        if(viewport != null) {
            GLES20.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(TEXTURE_2D_BINDABLE, texID);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, 0);

        if(mProgram != null) {
            mProgram.bind();
        }
        GLES20.glDrawArrays(DRAW_FUNCTION, 0, 4);
//        GLES20.glUniform1f(0, 0.1f);
    }

    @Override
    public void setTextureSize(int w, int h) {
        mTextureWidth = w;
        mTextureHeight = h;
    }

    @Override
    public String getVertexShaderString() {
        return vshDrawDefault;
    }

    @Override
    public String getFragmentShaderString() {
        return fshDrawOrigin;
    }
}
