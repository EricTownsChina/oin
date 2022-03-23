package priv.eric.oin.danmaku.service;

import priv.eric.oin.common.entity.Resp;

/**
 * Desc: 弹幕service
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/13 22:20
 */
public interface DanmakuService {

    /**
     * 获取到弹幕文件
     *
     * @return 标准返回
     */
    Resp fetchDanmakuFile();

}
