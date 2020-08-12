package cn.edu.nju.software.sda.app.TestSPI;

import cn.edu.nju.software.sda.plugin.Plugin;

/**
 * @Auther: yaya
 * @Date: 2019/12/9 15:17
 * @Description:
 */
public class TestPlugin implements Plugin {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDesc() {
        return null;
    }

    @Override
    public void install() {
        System.out.println("SPI install");
    }

    @Override
    public void uninstall() {
        System.out.println("SPI uninstall");

    }
}
