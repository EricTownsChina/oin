package priv.eric.oin.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.eric.oin.entity.Resp;
import priv.eric.oin.robot.BilibiliBvRobot;
import priv.eric.oin.robot.BilibiliCommentRobot;

import javax.annotation.Resource;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-17 11:12
 * <p>
 * desc:
 */
@RestController
@RequestMapping("/robot/bilibili/comment")
public class BilibiliCommentRobotController {

    @Resource
    private BilibiliCommentRobot bilibiliCommentRobot;
    @Resource
    private BilibiliBvRobot bilibiliBvRobot;

    @RequestMapping("/testSaveXml/{bvCode}")
    public Resp saveBvCommentXml(@PathVariable String bvCode) {
        bilibiliCommentRobot.saveBvComment(bvCode);
        return Resp.ok();
    }

    @RequestMapping("/executeBilibiliRobot")
    public Resp saveDataSource() {
        bilibiliBvRobot.handleBvHtml();
        return Resp.ok();
    }

}
