package priv.eric.oin.service;

import java.util.Deque;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/12 20:54
 * <p>
 * Desc: BV号相关的service
 */
public interface BvCodeService {

    /**
     * 获取一个网页中所有的BV号
     *
     * @param url b站网页
     * @return BV号列表
     */
    Deque<String> getBvCodesByUrl(String url);



}
