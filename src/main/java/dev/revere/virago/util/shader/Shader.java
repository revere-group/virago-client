package dev.revere.virago.util.shader;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * @author Remi
 * @project Virago
 * @date 3/19/2024
 */
@Getter
public class Shader {
    private final int fragID;
    private final int vertID;
    private final int program;
    private final Map<String, Integer> uniformLocationMap = new HashMap<>();

    /**
     * Shader constructor to initialize the shader.
     *
     * @param fragment the fragment
     */
    public Shader(ResourceLocation fragment) {
        this.vertID = this.createShader(this.readResourceLocation(new ResourceLocation("virago/shader/vertex.vert")), 35633);
        this.fragID = this.createShader(this.readResourceLocation(fragment), 35632);
        if (this.vertID != 0 && this.fragID != 0) {
            this.program = ARBShaderObjects.glCreateProgramObjectARB();
            if (this.program != 0) {
                ARBShaderObjects.glAttachObjectARB(this.program, this.vertID);
                ARBShaderObjects.glAttachObjectARB(this.program, this.fragID);
                ARBShaderObjects.glLinkProgramARB(this.program);
                ARBShaderObjects.glValidateProgramARB(this.program);
            }
        } else {
            this.program = -1;
        }
    }

    public void init() {
        GL20.glUseProgram(this.getProgram());
    }

    /**
     * Bind the shader
     *
     * @param x x position
     * @param y y position
     * @param h height
     * @param w width
     */
    public void bind(float x, float y, float h, float w) {
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(x, (y + h));
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f((x + w), (y + h));
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f((x + w), y);
        GL11.glEnd();
    }

    public void finish() {
        GL20.glUseProgram(0);
    }

    /**
     * Set the uniform
     *
     * @param uniform the uniform
     */
    public void setupUniform(String uniform) {
        this.uniformLocationMap.put(uniform, GL20.glGetUniformLocation(this.program, (CharSequence)uniform));
    }

    /**
     * Get the uniform
     *
     * @param uniform the uniform
     * @return the uniform
     */
    public int getUniform(String uniform) {
        return this.uniformLocationMap.get(uniform);
    }

    /**
     * Set the uniform
     *
     * @param name the name
     * @param args the args
     */
    public void setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(program, name);
        switch (args.length) {
            case 1:
                glUniform1f(loc, args[0]);
                break;
            case 2:
                glUniform2f(loc, args[0], args[1]);
                break;
            case 3:
                glUniform3f(loc, args[0], args[1], args[2]);
                break;
            case 4:
                glUniform4f(loc, args[0], args[1], args[2], args[3]);
                break;
        }
    }

    /**
     * Set the uniform
     *
     * @param name the name
     * @param args the args
     */
    public void setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(program, name);
        if (args.length > 1) glUniform2i(loc, args[0], args[1]);
        else glUniform1i(loc, args[0]);
    }


    /**
     * Read resource location
     *
     * @param loc the location
     * @return the location
     */
    private String readResourceLocation(ResourceLocation loc) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * Create shader
     *
     * @param source the source
     * @param type the type
     * @return the shader
     */
    private int createShader(String source, int type) {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(type);
            if (shader != 0) {
                ARBShaderObjects.glShaderSourceARB(shader, (CharSequence)source);
                ARBShaderObjects.glCompileShaderARB(shader);
                if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0) {
                    throw new RuntimeException("Error creating shader: " + ARBShaderObjects.glGetInfoLogARB(shader, ARBShaderObjects.glGetObjectParameteriARB(shader, 35716)));
                }
                return shader;
            }
            return 0;
        }
        catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            e.printStackTrace();
            throw e;
        }
    }
}
