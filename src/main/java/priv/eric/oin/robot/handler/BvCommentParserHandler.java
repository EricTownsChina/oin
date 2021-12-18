package priv.eric.oin.robot.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import priv.eric.oin.dao.BilibiliCommentDao;
import priv.eric.oin.entity.BvComment;
import priv.eric.oin.util.UUIDUtil;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-16 23:05
 * <p>
 * desc:
 */
@Slf4j
@Service
public class BvCommentParserHandler extends DefaultHandler {

    private String bvCode;
    private String cid;

    public String getBvCode() {
        return bvCode;
    }

    public void setBvCode(String bvCode) {
        this.bvCode = bvCode;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    private final AtomicInteger docIndex = new AtomicInteger();

    private static final String D = "d";
    private boolean handleFlag = false;
    private BvComment bvComment;

    @Resource
    private BilibiliCommentDao bilibiliCommentDao;

    @Override
    public void startDocument() throws SAXException {
        log.info("cid = {} 的弹幕xml解析开始.", cid);
    }

    @Override
    public void endDocument() throws SAXException {
        log.info("cid = {} 的弹幕xml解析结束, 共解析到 {} 个弹幕.", cid, docIndex);
    }

    /**
     * 节点属性
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //调用父类的解析元素方法
        super.startElement(uri, localName, qName, attributes);
        if (!D.equals(qName)) {
            handleFlag = false;
            return;
        }
        bvComment = new BvComment();
        bvComment.setBvCode(bvCode);
        bvComment.setCid(cid);
        // 填充弹幕信息
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            String keyName = attributes.getLocalName(i);
            if ("p".equals(keyName)) {
                // 解析弹幕在BV中的偏移量
                String infoStr = attributes.getValue(i);
                int timeStr = Integer.parseInt(infoStr.substring(0, infoStr.indexOf(".")));
                int min = timeStr / 60;
                int sec = timeStr % 60;
                // 个位数进行前补零
                bvComment.setOffset(String.format("%02d", min) + ":" + String.format("%02d", sec));

                // 提取弹幕ID
                // 382.56300,1,25,16777215,1638774676,0,63e27107,58554306278730240,10
                String[] values = infoStr.split(",");
                if (values.length == 9) {
                    String commentId = values[7];
                    if (StringUtils.isEmpty(commentId)) {
                        return;
                    }
                    bvComment.setCommentId(Long.parseLong(commentId));
                }
            }
        }
        handleFlag = true;
    }

    /**
     * 节点内容
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        // 不是<p>或者没有弹幕ID
        if (!handleFlag || null == bvComment || 0L == bvComment.getCommentId()) {
            return;
        }

        // 弹幕内容
        bvComment.setContent(new String(ch, start, length));
        // 创建时间
        bvComment.setCreateTime(new Timestamp(System.currentTimeMillis()));
        // ID
        bvComment.setId(UUIDUtil.next());

        // 入库
        bilibiliCommentDao.addNewComment(bvComment);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        docIndex.getAndIncrement();
    }

}
