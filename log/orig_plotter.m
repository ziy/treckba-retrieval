labels = data(1:2:11, 2)
subplot(2,4,1), plot(data(1:11, 3:5), '-o'), 
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(a) BOW - Const: 0.5')
subplot(2,4,2), plot(data(12:22, 3:5), '-o'),
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(b) BOW - Const: 1')
subplot(2,4,3), plot(data(23:33, 3:5), '-o'),
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(c) BOW - Const: 2')
subplot(2,4,4), plot(data(34:44, 3:5), '-o'),
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(d) BOW - Min-Max')
subplot(2,4,5), plot(data(45:55, 3:5), '-o'),
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(e) Phrase - Const: 0.5')
subplot(2,4,6), plot(data(56:66, 3:5), '-o'),
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(f) Phrase - Const: 1')
subplot(2,4,7), plot(data(67:77, 3:5), '-o'),
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(g) Phrase - Const: 2')
subplot(2,4,8), plot(data(78:88, 3:5), '-o'),
set(gca,'XTick',1:2:11), set(gca,'XTickLabel',labels)
axis([1,11,0,1])
title('(e) Phrase - Min-Max')
