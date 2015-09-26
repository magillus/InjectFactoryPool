# InjectFactoryPool
Test of idea not creating new views during inflation, but use pool of views that are available and adapting its styles in place of calling new view, which should save possible GC calls on inflation.
## progress...
After initial proof of concept it works but styles from attributes are not applied correctly. Main reason for that is the way Views process style attributes - only in constructor.
To work this around is to create Pooled Views types that can adapt its Views based on styles - those Pooled views would be mostly copy of original Views with public methods to update styles based on attribute set.

example:
    PooledTextView extends TextView {
      public PooledTextView(Context context, AttributesSet attrs) {
        ....
        updateAttributes(attrs);
      }
    
      public void resetToDefaultAttributes() {
        ... resets view to default attributes.
      }
      public void updateAttributes(AttributeSet attrs) {
        ... logic to apply attribute changes
      }
    }
