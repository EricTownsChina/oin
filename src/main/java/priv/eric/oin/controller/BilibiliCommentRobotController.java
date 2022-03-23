package priv.eric.oin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.eric.oin.common.entity.Resp;
import priv.eric.oin.robot.BilibiliCommentRobot;
import priv.eric.oin.robot.BvCommentRobot;

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

    private static final String BV_URL = "https://www.bilibili.com";

    @Resource
    private BilibiliCommentRobot bilibiliCommentRobot;
    @Resource
    private BvCommentRobot bvCommentRobot;

    @RequestMapping("/testSaveXml/{bvCode}")
    public Resp saveBvCommentXml(@PathVariable String bvCode) {
        bilibiliCommentRobot.saveBvComment(bvCode);
        return Resp.ok();
    }

    @RequestMapping("/tryHandle")
    public Resp tryHandleBvComment() {
        bvCommentRobot.saveCommentFiles(BV_URL);
        return Resp.ok();
    }

}
